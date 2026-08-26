package io.github.finalwave.controller;

import io.github.finalwave.model.command.ZombotanyMenuCommands;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.MatchListener;
import io.github.finalwave.model.game.MatchResult;
import io.github.finalwave.model.game.board.PlantPlacementResult;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.zombie.Zombie;
import io.github.finalwave.model.item.SunType;
import io.github.finalwave.model.minigame.MiniGameStageConfig;
import io.github.finalwave.model.minigame.mode.ZombotanyMode;
import io.github.finalwave.model.user.UnlockService;
import io.github.finalwave.model.user.User;
import io.github.finalwave.model.user.UserDatabase;
import io.github.finalwave.view.api.minigame.ZombotanyView;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;

public class ZombotanyController extends ViewController implements MatchListener {

    private final User user;
    private final UserDatabase userDatabase;
    private final ZombotanyMode mode;
    private final GameSession session;
    private final MiniGameStageConfig stage;
    private final Set<String> boostedPlants;
    private final UnlockService unlockService = new UnlockService();
    private boolean finishedHandled;
    private boolean deferMatchExit;

    public ZombotanyController(User user,
                               UserDatabase userDatabase,
                               ZombotanyMode mode,
                               GameSession session,
                               MiniGameStageConfig stage) {
        this(user, userDatabase, mode, session, stage, Set.of());
    }

    public ZombotanyController(User user,
                               UserDatabase userDatabase,
                               ZombotanyMode mode,
                               GameSession session,
                               MiniGameStageConfig stage,
                               Set<String> boostedPlants) {
        this.user = user;
        this.userDatabase = userDatabase;
        this.mode = mode;
        this.session = session;
        this.stage = stage;
        this.boostedPlants = boostedPlants == null ? Set.of() : Set.copyOf(boostedPlants);
        this.session.setMatchListener(this);
    }

    public Set<String> boostedPlants() {
        return boostedPlants;
    }

    @Override
    public void displayMenu() {
        getViewApi().showStageStarted(
                stage.getStageIndex(),
                stage.getStartingSun(),
                stage.getPlantSeedPool());
        getViewApi().showSunAmount(session.getSunBalance());
    }

    @Override
    public void handleCommand(String input) {
        for (ZombotanyMenuCommands cmd : ZombotanyMenuCommands.values()) {
            Matcher matcher = cmd.getMatcher(input);
            if (matcher == null) {
                continue;
            }
            switch (cmd) {
                case ADVANCE_TIME -> handleAdvanceTime(matcher.group("count"));
                case COLLECT_SUN -> handleCollectSun(matcher.group("x"), matcher.group("y"));
                case SHOW_SUN_AMOUNT -> getViewApi().showSunAmount(session.getSunBalance());
                case PLANT_PLANT -> handlePlantPlant(
                        matcher.group("type"), matcher.group("x"), matcher.group("y"));
                case PLUCK_PLANT -> handlePluckPlant(matcher.group("x"), matcher.group("y"));
                case SHOW_MAP -> getViewApi().showMap(session.renderMap());
                case ZOMBIES_INFO -> getViewApi().showZombiesInfo(session.getZombies());
                case MENU_SHOW_CURRENT -> getViewApi().showCurrentMenu();
                case MENU_EXIT -> {
                    session.stop();
                    navigator.pop();
                    return;
                }
            }
            maybeReturnAfterMatch();
            return;
        }
        getViewApi().errorInvalidCommand();
    }

    private void handleAdvanceTime(String count) {
        int ticks;
        try {
            ticks = Integer.parseInt(count);
        } catch (NumberFormatException e) {
            getViewApi().errorInvalidTickCount();
            return;
        }
        advance(ticks);
    }

    private void handleCollectSun(String x, String y) {
        collectSunAt(parseCoord(x), parseCoord(y));
    }

    private void handlePlantPlant(String type, String x, String y) {
        plantAt(type, parseCoord(x), parseCoord(y));
    }

    private void handlePluckPlant(String x, String y) {
        shovelAt(parseCoord(x), parseCoord(y));
    }

    private void maybeReturnAfterMatch() {
        if (finishedHandled) {
            return;
        }
        MatchResult result = session.getMatchResult();
        if (result != MatchResult.WON && result != MatchResult.LOST) {
            return;
        }
        finishedHandled = true;
        recordFinishedGame();
        if (result == MatchResult.WON) {
            user.getMiniGameProgress().markStageCompleted(
                    stage.getMiniGameId(), stage.getStageIndex());
            userDatabase.saveMiniGameProgress(user);
        }
        if (deferMatchExit) {
            return;
        }
        navigator.pop();
    }

    private void recordFinishedGame() {
        user.recordGamePlayed();
        userDatabase.saveGamesPlayed(user);
    }

    private int parseCoord(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private ZombotanyView getViewApi() {
        return (ZombotanyView) view;
    }

    public void cheatAddSun(int amount) {
        if (amount <= 0 || session == null) {
            return;
        }
        session.addSunBalance(amount);
        getViewApi().showSunAmount(session.getSunBalance());
        maybeReturnAfterMatch();
    }

    public void cheatAddPlantFood() {
        if (session == null) {
            return;
        }
        session.addPlantFood(1);
        maybeReturnAfterMatch();
    }

    public GameSession session() {
        return session;
    }

    public GameSession getSession() {
        return session;
    }

    public User getUser() {
        return user;
    }

    public MiniGameStageConfig getStage() {
        return stage;
    }

    public ZombotanyMode getMode() {
        return mode;
    }

    public void setDeferMatchExit(boolean deferMatchExit) {
        this.deferMatchExit = deferMatchExit;
    }

    public void confirmMatchExit() {
        navigator.pop();
    }

    public void restartMatch() {
        navigator.pop();
    }

    public void advance(int ticks) {
        if (ticks < 0) {
            getViewApi().errorNegativeTickCount();
            return;
        }
        session.advanceTicks(ticks);
        getViewApi().showAdvanceTime(ticks);
        maybeReturnAfterMatch();
    }

    public PlantPlacementResult plantAt(String plantName, int col, int row) {
        if (plantName == null || plantName.isBlank()) {
            getViewApi().errorPlantNotFound(plantName);
            return PlantPlacementResult.UNKNOWN_PLANT;
        }
        if (col < 0 || row < 0) {
            getViewApi().errorInvalidLocation(col, row);
            return PlantPlacementResult.OUT_OF_BOUNDS;
        }
        String plantType = plantName.trim();
        int plantLevel = user.getPlantProgress().getOwnedPlant(plantType)
                .map(owned -> owned.getLevel())
                .orElse(1);
        PlantPlacementResult result = session.tryPlant(plantType, col, row, plantLevel);
        switch (result) {
            case SUCCESS -> {
                getViewApi().showPlantPlanted(plantType, col, row);
                if (boostedPlants.contains(plantType)) {
                    Plant planted = session.getBoard().getPlantAt(col, row);
                    if (planted != null) {
                        planted.activatePlantFoodEffect(session.getContext());
                    }
                }
            }
            case UNKNOWN_PLANT -> getViewApi().errorPlantNotFound(plantType);
            case NOT_IN_LOADOUT -> getViewApi().errorPlantNotSelected(plantType);
            case ON_COOLDOWN -> getViewApi().errorPlantOnCooldown(plantType);
            case INSUFFICIENT_SUN -> getViewApi().errorNotEnoughSun();
            case OUT_OF_BOUNDS -> getViewApi().errorInvalidLocation(col, row);
            default -> getViewApi().errorCannotPlantHere(col, row);
        }
        maybeReturnAfterMatch();
        return result;
    }

    public boolean collectSunAt(int col, int row) {
        if (col < 0 || row < 0) {
            getViewApi().errorInvalidLocation(col, row);
            return false;
        }
        if (!session.collectSunAt(col, row)) {
            getViewApi().errorNoSunAt(col, row);
            return false;
        }
        return true;
    }

    public boolean shovelAt(int col, int row) {
        if (col < 0 || row < 0) {
            getViewApi().errorInvalidLocation(col, row);
            return false;
        }
        if (!session.pluckPlant(col, row)) {
            getViewApi().errorNoPlantToPluck(col, row);
            return false;
        }
        getViewApi().showPlantPlucked(col, row);
        maybeReturnAfterMatch();
        return true;
    }

    public boolean feedAt(int col, int row) {
        if (session.getPlantFoodCount() <= 0) {
            return false;
        }
        if (!session.usePlantFood(col, row)) {
            getViewApi().errorCannotPlantHere(col, row);
            return false;
        }
        maybeReturnAfterMatch();
        return true;
    }

    @Override
    public void onWin() {
        getViewApi().showWinMessage();
    }

    @Override
    public void onLose() {
        getViewApi().showLoseMessage();
    }

    @Override
    public void onPlantDestroyed(Plant plant, int x, int y) {
    }

    @Override
    public void onSunDropped(SunType type, int x, int y) {
    }

    @Override
    public void onLawnMowerTriggered(int row, List<Zombie> killed) {
    }

    @Override
    public void onZombieSpawned(String type, int wave, int lane, int cost) {
        ZombieSeenUnlock.unlock(user, userDatabase, unlockService, type);
    }

    @Override
    public void onZombieDied(String type, double x, double y) {
    }
}
