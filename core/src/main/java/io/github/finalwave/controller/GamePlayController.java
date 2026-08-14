package io.github.finalwave.controller;

import io.github.finalwave.model.adventure.ChapterConfig;
import io.github.finalwave.model.adventure.ChapterId;
import io.github.finalwave.model.adventure.LevelConfig;
import io.github.finalwave.model.command.GamePlayMenuCommands;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.MatchListener;
import io.github.finalwave.model.game.MatchResult;
import io.github.finalwave.model.game.board.PlantPlacementResult;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.zombie.Zombie;
import io.github.finalwave.model.game.mode.AdventureMode;
import io.github.finalwave.model.item.SunType;
import io.github.finalwave.model.minigame.MiniGameId;
import io.github.finalwave.model.quest.QuestTracker;
import io.github.finalwave.model.user.ChapterProgress;
import io.github.finalwave.model.user.UnlockService;
import io.github.finalwave.model.user.User;
import io.github.finalwave.model.user.UserDatabase;
import io.github.finalwave.view.api.GamePlayView;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;

public class GamePlayController extends ViewController implements MatchListener {

    private final User user;
    private final UserDatabase userDatabase;
    private final AdventureMode adventureMode;
    private final GameSession session;
    private final ChapterConfig chapter;
    private final LevelConfig level;
    private final Set<String> boostedPlants;
    private final UnlockService unlockService = new UnlockService();
    private final boolean awardAdventureProgress;
    private boolean finishedHandled;
    private boolean deferMatchExit;

    public GamePlayController(User user,
                              UserDatabase userDatabase,
                              AdventureMode adventureMode,
                              GameSession session,
                              ChapterConfig chapter,
                              LevelConfig level,
                              Set<String> boostedPlants) {
        this(user, userDatabase, adventureMode, session, chapter, level, boostedPlants, true);
    }

    protected GamePlayController(User user,
                                 UserDatabase userDatabase,
                                 AdventureMode adventureMode,
                                 GameSession session,
                                 ChapterConfig chapter,
                                 LevelConfig level,
                                 Set<String> boostedPlants,
                                 boolean awardAdventureProgress) {
        this.user = user;
        this.userDatabase = userDatabase;
        this.adventureMode = adventureMode;
        this.session = session;
        this.chapter = chapter;
        this.level = level;
        this.boostedPlants = boostedPlants == null ? Set.of() : Set.copyOf(boostedPlants);
        this.awardAdventureProgress = awardAdventureProgress;
        this.session.setMatchListener(this);
    }

    public User getUser() {
        return user;
    }

    public GameSession session() {
        return session;
    }

    public Set<String> boostedPlants() {
        return boostedPlants;
    }

    public ChapterConfig chapter() {
        return chapter;
    }

    public LevelConfig level() {
        return level;
    }

    public void setDeferMatchExit(boolean deferMatchExit) {
        this.deferMatchExit = deferMatchExit;
    }

    public void back() {
        navigator.pop();
    }

    public void confirmMatchExit() {
        navigator.pop();
    }

    public void restartMatch() {
        navigator.pop();
    }

    public void requestPause() {
    }

    public void advance(int ticks) {
        if (ticks < 0) {
            getGamePlayView().errorNegativeTickCount();
            return;
        }
        session.advanceTicks(ticks);
        getGamePlayView().showAdvanceTime(ticks);
        maybeReturnAfterMatch();
    }

    public boolean collectSunAt(int col, int row) {
        if (col < 0 || row < 0) {
            getGamePlayView().errorInvalidLocation(col, row);
            return false;
        }
        if (!session.collectSunAt(col, row)) {
            getGamePlayView().errorNoSunAt(col, row);
            return false;
        }
        maybeReturnAfterMatch();
        return true;
    }

    public PlantPlacementResult plantAt(String plantName, int col, int row) {
        if (plantName == null || plantName.isBlank()) {
            getGamePlayView().errorPlantNotFound(plantName);
            return PlantPlacementResult.UNKNOWN_PLANT;
        }
        if (col < 0 || row < 0) {
            getGamePlayView().errorInvalidLocation(col, row);
            return PlantPlacementResult.OUT_OF_BOUNDS;
        }
        int plantLevel = user.getPlantProgress().getOwnedPlant(plantName)
                .map(owned -> owned.getLevel())
                .orElse(1);
        PlantPlacementResult result = session.tryPlant(plantName, col, row, plantLevel);
        switch (result) {
            case SUCCESS -> {
                getGamePlayView().showPlantPlanted(plantName, col, row);
                if (boostedPlants.contains(plantName)) {
                    Plant planted = session.getBoard().getPlantAt(col, row);
                    if (planted != null) {
                        planted.activatePlantFoodEffect(session.getContext());
                    }
                }
            }
            case UNKNOWN_PLANT -> getGamePlayView().errorPlantNotFound(plantName);
            case NOT_IN_LOADOUT -> getGamePlayView().errorPlantNotSelected(plantName);
            case NOT_ON_CONVEYOR_BELT -> getGamePlayView().errorPlantNotOnConveyorBelt(plantName);
            case LEVEL_PLANT_LOCKED -> getGamePlayView().errorLevelPlantLocked(plantName);
            case ON_COOLDOWN -> getGamePlayView().errorPlantOnCooldown(plantName);
            case INSUFFICIENT_SUN -> getGamePlayView().errorNotEnoughSun();
            case OUT_OF_BOUNDS -> getGamePlayView().errorInvalidLocation(col, row);
            default -> getGamePlayView().errorCannotPlantHere(col, row);
        }
        maybeReturnAfterMatch();
        return result;
    }

    public boolean shovelAt(int col, int row) {
        Plant plant = session.getBoard().getPlantAt(col, row);
        if (plant != null && session.isProtectedSeed(plant)) {
            getGamePlayView().errorCannotPluckProtectedSeed(col, row);
            return false;
        }
        if (!session.pluckPlant(col, row)) {
            getGamePlayView().errorNoPlantToPluck(col, row);
            return false;
        }
        getGamePlayView().showPlantPlucked(col, row);
        maybeReturnAfterMatch();
        return true;
    }

    public boolean feedAt(int col, int row) {
        if (session.getPlantFoodCount() <= 0) {
            getGamePlayView().errorNoPlantFood();
            return false;
        }
        if (!session.usePlantFood(col, row)) {
            getGamePlayView().errorCannotFeedHere(col, row);
            return false;
        }
        getGamePlayView().showPlantFed(col, row);
        maybeReturnAfterMatch();
        return true;
    }

    public void startWaves() {
        session.startZombieWaves();
        maybeReturnAfterMatch();
    }

    public void cheatAddSun(int amount) {
        session.addSunBalance(amount);
        getGamePlayView().showCheatAddedSuns(amount);
        maybeReturnAfterMatch();
    }

    public void cheatAddPlantFood() {
        session.addPlantFood(1);
        getGamePlayView().showCheatAddedPlantFood();
        maybeReturnAfterMatch();
    }

    public void cheatRemoveCooldown() {
        session.removeAllCooldowns();
        getGamePlayView().showCheatCooldownRemoved();
    }

    public void cheatNuke() {
        session.nukeAllZombies();
        getGamePlayView().showNukeActivated();
        maybeReturnAfterMatch();
    }

    @Override
    public void onExit() {
        detachQuestTracker();
    }

    @Override
    public void displayMenu() {
        if (view instanceof GamePlayView gamePlayView) {
            gamePlayView.showSunAmount(session.getSunBalance());
        }
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
            navigator.pop();
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
        advance(ticks);
    }

    private void handleCollectSun(String x, String y) {
        collectSunAt(parseCoord(x), parseCoord(y));
    }

    private void handleShowSunAmount() {
        getGamePlayView().showSunAmount(session.getSunBalance());
    }

    private void handlePlantPlant(String type, String x, String y) {
        plantAt(type.trim(), parseCoord(x), parseCoord(y));
    }

    private void handlePluckPlant(String x, String y) {
        shovelAt(parseCoord(x), parseCoord(y));
    }

    private void handleFeedPlant(String x, String y) {
        feedAt(parseCoord(x), parseCoord(y));
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
        startWaves();
    }

    private void handleCheatAddSuns(String count) {
        int amount;
        try {
            amount = Integer.parseInt(count);
        } catch (NumberFormatException e) {
            getGamePlayView().errorInvalidSunCount();
            return;
        }
        cheatAddSun(amount);
    }

    private void handleReleaseTheNuke() {
        cheatNuke();
    }

    private void handleCheatRemoveCooldown() {
        cheatRemoveCooldown();
    }

    private void handleCheatAddPlantFood() {
        cheatAddPlantFood();
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
        if (result == MatchResult.WON || result == MatchResult.LOST) {
            finishedHandled = true;
            onMatchFinished(result);
        }
    }

    protected void onMatchFinished(MatchResult result) {
        recordFinishedGame();
        if (result == MatchResult.WON && awardAdventureProgress && chapter != null && level != null) {
            ChapterProgress.LevelCompletionResult completion =
                    user.getChapterProgress().markLevelCompleted(chapter.getId(), level.getIndex());
            userDatabase.saveAdventureProgress(user);
            publishUnlockNews(completion);
        }
        if (deferMatchExit) {
            if (result == MatchResult.WON) {
                getGamePlayView().showWinMessage();
            } else {
                getGamePlayView().showLoseMessage();
            }
            return;
        }
        navigator.pop();
    }

    private void publishUnlockNews(ChapterProgress.LevelCompletionResult completion) {
        boolean newlyUnlocked = false;
        if (completion.newlyUnlockedChapter().isPresent()) {
            ChapterId next = completion.newlyUnlockedChapter().get();
            if (unlockService.unlockLevel(user, levelId(next))) {
                newlyUnlocked = true;
            }
            MiniGameId[] minigames = MiniGameId.values();
            if (next.ordinal() < minigames.length
                    && unlockService.unlockMinigame(user, minigames[next.ordinal()].getKey())) {
                newlyUnlocked = true;
            }
        }
        if (completion.completedFinalChapterGate()) {
            MiniGameId[] minigames = MiniGameId.values();
            MiniGameId last = minigames[minigames.length - 1];
            if (unlockService.unlockMinigame(user, last.getKey())) {
                newlyUnlocked = true;
            }
        }
        if (newlyUnlocked) {
            userDatabase.saveUserWallet(user);
        }
    }

    static String levelId(ChapterId chapterId) {
        return (chapterId.ordinal() + 1) + "-1";
    }

    protected void recordFinishedGame() {
        user.recordGamePlayed();
        userDatabase.saveGamesPlayed(user);
    }

    protected UserDatabase getUserDatabase() {
        return userDatabase;
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
                new io.github.finalwave.model.item.Sun(x, y, 0, type, false), x, y);
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
        ZombieSeenUnlock.unlock(user, userDatabase, unlockService, type);
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
            io.github.finalwave.model.user.GreenhousePot pot = user.findNextLockedPot();
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
        getGamePlayView().showLoseMessage();
    }
}
