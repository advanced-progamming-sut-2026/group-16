package controller;

import model.adventure.ChapterConfig;
import model.adventure.LevelConfig;
import model.command.GamePlayMenuCommands;
import model.game.GameSession;
import model.game.MatchListener;
import model.game.MatchResult;
import model.game.board.PlantPlacementResult;
import model.game.entity.plant.Plant;
import model.game.entity.zombie.Zombie;
import model.game.mode.AdventureMode;
import model.item.SunType;
import model.quest.QuestTracker;
import model.user.UnlockService;
import model.user.User;
import model.user.UserDatabase;
import view.api.GamePlayView;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;

public class GamePlayController extends ViewController implements MatchListener {

    private final User user;
    private final UserDatabase userDatabase;
    private final AdventureController adventureController;
    private final AdventureMode adventureMode;
    private final GameSession session;
    private final ChapterConfig chapter;
    private final LevelConfig level;
    private final Set<String> boostedPlants;
    private final UnlockService unlockService = new UnlockService();
    private boolean finishedHandled;

    public GamePlayController(User user,
                              UserDatabase userDatabase,
                              AdventureController adventureController,
                              AdventureMode adventureMode,
                              GameSession session,
                              ChapterConfig chapter,
                              LevelConfig level,
                              Set<String> boostedPlants) {
        this.user = user;
        this.userDatabase = userDatabase;
        this.adventureController = adventureController;
        this.adventureMode = adventureMode;
        this.session = session;
        this.chapter = chapter;
        this.level = level;
        this.boostedPlants = boostedPlants == null ? Set.of() : Set.copyOf(boostedPlants);
        this.session.setMatchListener(this);
    }

    @Override
    public void displayMenu() {
        getGamePlayView().showSunAmount(session.getSunBalance());
    }

    @Override
    public void handleCommand(String input) {
        for (GamePlayMenuCommands cmd : GamePlayMenuCommands.values()) {
            Matcher matcher = cmd.getMatcher(input);
            if (matcher == null) {
                continue;
            }
            switch (cmd) {
                case ADVANCE_TIME -> handleAdvanceTime(matcher.group("count"));
                case COLLECT_SUN -> handleCollectSun(matcher.group("x"), matcher.group("y"));
                case SHOW_SUN_AMOUNT -> handleShowSunAmount();
                case PLANT_PLANT -> handlePlantPlant(matcher.group("type"), matcher.group("x"),
                        matcher.group("y"));
                case PLUCK_PLANT -> handlePluckPlant(matcher.group("x"), matcher.group("y"));
                case FEED_PLANT -> handleFeedPlant(matcher.group("x"), matcher.group("y"));
                case SHOW_MAP -> handleShowMap();
                case SHOW_PLANTS_STATUS -> handleShowPlantsStatus();
                case SHOW_TILE_STATUS -> handleShowTileStatus(matcher.group("x"), matcher.group("y"));
                case ZOMBIES_INFO -> handleZombiesInfo();
                case START_ZOMBIE_WAVES -> handleStartZombieWaves();
                case CHEAT_ADD_SUNS -> handleCheatAddSuns(matcher.group("count"));
                case RELEASE_THE_NUKE -> handleReleaseTheNuke();
                case CHEAT_REMOVE_COOLDOWN -> handleCheatRemoveCooldown();
                case CHEAT_ADD_PLANT_FOOD -> handleCheatAddPlantFood();
                case CHEAT_ADD_ZOMBIE -> handleCheatAddZombie(matcher.group("zombieType"),
                        matcher.group("x"), matcher.group("y"));
            }
            maybeReturnAfterMatch();
            return;
        }
        if ("menu exit".equalsIgnoreCase(input.trim())) {
            detachQuestTracker();
            parser.switchController(adventureController);
            return;
        }
        getGamePlayView().errorInvalidCommand();
    }

    private void handleAdvanceTime(String count) {
        int ticks;
        try {
            ticks = Integer.parseInt(count);
        } catch (NumberFormatException e) {
            getGamePlayView().errorInvalidTickCount();
            return;
        }
        if (ticks < 0) {
            getGamePlayView().errorNegativeTickCount();
            return;
        }
        session.advanceTicks(ticks);
        getGamePlayView().showAdvanceTime(ticks);
    }

    private void handleCollectSun(String x, String y) {
        int col = parseCoord(x);
        int row = parseCoord(y);
        if (col < 0 || row < 0) {
            getGamePlayView().errorInvalidLocation(col, row);
            return;
        }
        if (!session.collectSunAt(col, row)) {
            getGamePlayView().errorNoSunAt(col, row);
        }
    }

    private void handleShowSunAmount() {
        getGamePlayView().showSunAmount(session.getSunBalance());
    }

    private void handlePlantPlant(String type, String x, String y) {
        String plantType = type.trim();
        int col = parseCoord(x);
        int row = parseCoord(y);
        if (col < 0 || row < 0) {
            getGamePlayView().errorInvalidLocation(col, row);
            return;
        }
        int plantLevel = user.getPlantProgress().getOwnedPlant(plantType)
                .map(owned -> owned.getLevel())
                .orElse(1);
        PlantPlacementResult result = session.tryPlant(plantType, col, row, plantLevel);
        switch (result) {
            case SUCCESS -> {
                getGamePlayView().showPlantPlanted(plantType, col, row);
                if (boostedPlants.contains(plantType)) {
                    Plant planted = session.getBoard().getPlantAt(col, row);
                    if (planted != null) {
                        planted.activatePlantFoodEffect(session.getContext());
                    }
                }
            }
            case UNKNOWN_PLANT -> getGamePlayView().errorPlantNotFound(plantType);
            case NOT_IN_LOADOUT -> getGamePlayView().errorPlantNotSelected(plantType);
            case NOT_ON_CONVEYOR_BELT -> getGamePlayView().errorPlantNotOnConveyorBelt(plantType);
            case ON_COOLDOWN -> getGamePlayView().errorPlantOnCooldown(plantType);
            case INSUFFICIENT_SUN -> getGamePlayView().errorNotEnoughSun();
            case OUT_OF_BOUNDS -> getGamePlayView().errorInvalidLocation(col, row);
            default -> getGamePlayView().errorCannotPlantHere(col, row);
        }
    }

    private void handlePluckPlant(String x, String y) {
        int col = parseCoord(x);
        int row = parseCoord(y);
        if (!session.pluckPlant(col, row)) {
            getGamePlayView().errorNoPlantToPluck(col, row);
            return;
        }
        getGamePlayView().showPlantPlucked(col, row);
    }

    private void handleFeedPlant(String x, String y) {
        int col = parseCoord(x);
        int row = parseCoord(y);
        if (session.getPlantFoodCount() <= 0) {
            getGamePlayView().errorNoPlantFood();
            return;
        }
        if (!session.usePlantFood(col, row)) {
            getGamePlayView().errorCannotFeedHere(col, row);
            return;
        }
        getGamePlayView().showPlantFed(col, row);
    }

    private void handleShowMap() {
        getGamePlayView().showMap(session.renderMap());
    }

    private void handleShowPlantsStatus() {
        getGamePlayView().showPlantsStatus(session.renderPlantsStatus());
    }

    private void handleShowTileStatus(String x, String y) {
        int col = parseCoord(x);
        int row = parseCoord(y);
        getGamePlayView().showTileStatus(session.renderTileStatus(col, row));
    }

    private void handleZombiesInfo() {
        getGamePlayView().showZombiesInfo(session.getZombies());
    }

    private void handleStartZombieWaves() {
        session.startZombieWaves();
    }

    private void handleCheatAddSuns(String count) {
        int amount;
        try {
            amount = Integer.parseInt(count);
        } catch (NumberFormatException e) {
            getGamePlayView().errorInvalidSunCount();
            return;
        }
        session.addSunBalance(amount);
        getGamePlayView().showCheatAddedSuns(amount);
    }

    private void handleReleaseTheNuke() {
        session.nukeAllZombies();
        getGamePlayView().showNukeActivated();
    }

    private void handleCheatRemoveCooldown() {
        session.removeAllCooldowns();
        getGamePlayView().showCheatCooldownRemoved();
    }

    private void handleCheatAddPlantFood() {
        session.addPlantFood(1);
        getGamePlayView().showCheatAddedPlantFood();
    }

    private void handleCheatAddZombie(String zombieType, String x, String y) {
        double zx;
        int zy;
        try {
            zx = Double.parseDouble(x.trim());
            zy = Integer.parseInt(y.trim());
        } catch (NumberFormatException e) {
            getGamePlayView().errorInvalidZombieLocation();
            return;
        }
        try {
            session.spawnZombieOfType(zombieType.trim(), zy, zx);
            getGamePlayView().showCheatSpawnZombie(zombieType.trim(), zx, zy);
        } catch (RuntimeException ex) {
            getGamePlayView().errorZombieSpawnFailed(ex.getMessage());
        }
    }

    private void maybeReturnAfterMatch() {
        if (finishedHandled) {
            return;
        }
        MatchResult result = session.getMatchResult();
        if (result == MatchResult.WON) {
            finishedHandled = true;
            user.getChapterProgress().markLevelCompleted(chapter.getId(), level.getIndex());
            userDatabase.saveAdventureProgress(user);
            detachQuestTracker();
            parser.switchController(adventureController);
        } else if (result == MatchResult.LOST) {
            finishedHandled = true;
            detachQuestTracker();
            parser.switchController(adventureController);
        }
    }

    private void detachQuestTracker() {
        QuestTracker tracker = user.getQuestTracker();
        if (tracker == null) {
            return;
        }
        tracker.endSession();
        tracker.unregister();
        userDatabase.saveQuestProgress(user);
    }

    private int parseCoord(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private GamePlayView getGamePlayView() {
        return (GamePlayView) view;
    }

    protected GameSession getSession() {
        return session;
    }


    @Override
    public void onSunProduced(Plant plant, int x, int y) {
        getGamePlayView().showSunProduced(plant, x, y);
    }

    @Override
    public void onSunDropped(SunType type, int x, int y) {
        getGamePlayView().showSunDropped(
                new model.item.Sun(x, y, 0, type, false), x, y);
    }

    @Override
    public void onSunReachedGround(int x, int y) {
        getGamePlayView().showSunReachedGround(x, y);
    }

    @Override
    public void onPlantDestroyed(Plant plant, int x, int y) {
        getGamePlayView().showPlantDestroyed(plant, x, y);
    }

    @Override
    public void onLawnMowerTriggered(int row, List<Zombie> killed) {
        getGamePlayView().showLawnMowerTriggered(row);
        for (Zombie zombie : killed) {
            getGamePlayView().showLawnMowerKilledZombie(zombie.getType());
        }
    }

    @Override
    public void onLawnMowerFailed(int row) {
        getGamePlayView().showLawnMowerFailed(row);
    }

    @Override
    public void onWaveStarted(int waveNumber) {
        if (adventureMode != null) {
            adventureMode.onWaveStarted(session, waveNumber);
        }
        getGamePlayView().showWaveStarted(waveNumber);
    }

    @Override
    public void onFinalWave() {
        int waveNumber = session.getWaveManager() == null
                ? 0
                : session.getWaveManager().getCurrentWaveNumber();
        if (adventureMode != null) {
            adventureMode.onWaveStarted(session, Math.max(1, waveNumber));
        }
        getGamePlayView().showFinalWave();
    }

    @Override
    public void onZombieSpawned(String type, int wave, int lane, int cost) {
        if (unlockService.unlockZombie(user, type)) {
            userDatabase.saveUserWallet(user);
        }
        getGamePlayView().showZombieSpawned(type, wave, lane, cost);
    }

    @Override
    public void onZombieDied(String type, double x, double y) {
        getGamePlayView().showZombieDied(type, x, y);
    }

    @Override
    public void onGlowingZombieDroppedFood(int plantFoodCount) {
        getGamePlayView().showGlowingZombieDroppedFood(plantFoodCount);
    }

    @Override
    public void onItemDropped(String itemType, int amount) {
        if ("coin".equals(itemType)) {
            user.addCoins(amount);
            userDatabase.saveUserWallet(user);
            getGamePlayView().showItemDropped("coin", user.getCoins());
        } else if ("diamond".equals(itemType)) {
            user.addDiamonds(amount);
            userDatabase.saveUserWallet(user);
            getGamePlayView().showItemDropped("diamond", user.getDiamonds());
        } else if ("pot".equals(itemType)) {
            model.user.GreenhousePot pot = user.findNextLockedPot();
            if (pot != null) {
                pot.setLocked(false);
                userDatabase.saveUserWallet(user);
                getGamePlayView().showItemDropped("pot", user.countUnlockedPots());
            } else {
                getGamePlayView().showItemDropped("pot", user.countUnlockedPots());
            }
        }
    }

    @Override
    public void onGraveCreated(int col, int row, String lootType) {
        getGamePlayView().showGraveCreated(col, row, lootType);
    }

    @Override
    public void onWin() {
        getGamePlayView().showWinMessage();
    }

    @Override
    public void onLose() {

    }
}
