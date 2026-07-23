package controller;

import model.command.ZombotanyMenuCommands;
import model.game.GameSession;
import model.game.MatchListener;
import model.game.MatchResult;
import model.game.board.PlantPlacementResult;
import model.game.entity.plant.Plant;
import model.game.entity.zombie.Zombie;
import model.item.SunType;
import model.minigame.MiniGameStageConfig;
import model.minigame.mode.ZombotanyMode;
import model.user.User;
import model.user.UserDatabase;
import view.api.minigame.ZombotanyView;

import java.util.List;
import java.util.regex.Matcher;

public class ZombotanyController extends ViewController implements MatchListener {

    private final User user;
    private final UserDatabase userDatabase;
    private final MiniGameHubController hubController;
    private final ZombotanyMode mode;
    private final GameSession session;
    private final MiniGameStageConfig stage;
    private boolean finishedHandled;

    public ZombotanyController(User user,
                               UserDatabase userDatabase,
                               MiniGameHubController hubController,
                               ZombotanyMode mode,
                               GameSession session,
                               MiniGameStageConfig stage) {
        this.user = user;
        this.userDatabase = userDatabase;
        this.hubController = hubController;
        this.mode = mode;
        this.session = session;
        this.stage = stage;
        this.session.setMatchListener(this);
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
                    parser.switchController(hubController);
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
        if (ticks < 0) {
            getViewApi().errorNegativeTickCount();
            return;
        }
        session.advanceTicks(ticks);
        getViewApi().showAdvanceTime(ticks);
    }

    private void handleCollectSun(String x, String y) {
        int col = parseCoord(x);
        int row = parseCoord(y);
        if (col < 0 || row < 0) {
            getViewApi().errorInvalidLocation(col, row);
            return;
        }
        if (!session.collectSunAt(col, row)) {
            getViewApi().errorNoSunAt(col, row);
        }
    }

    private void handlePlantPlant(String type, String x, String y) {
        String plantType = type.trim();
        int col = parseCoord(x);
        int row = parseCoord(y);
        if (col < 0 || row < 0) {
            getViewApi().errorInvalidLocation(col, row);
            return;
        }
        int plantLevel = user.getPlantProgress().getOwnedPlant(plantType)
                .map(owned -> owned.getLevel())
                .orElse(1);
        PlantPlacementResult result = session.tryPlant(plantType, col, row, plantLevel);
        switch (result) {
            case SUCCESS -> getViewApi().showPlantPlanted(plantType, col, row);
            case UNKNOWN_PLANT -> getViewApi().errorPlantNotFound(plantType);
            case NOT_IN_LOADOUT -> getViewApi().errorPlantNotSelected(plantType);
            case ON_COOLDOWN -> getViewApi().errorPlantOnCooldown(plantType);
            case INSUFFICIENT_SUN -> getViewApi().errorNotEnoughSun();
            case OUT_OF_BOUNDS -> getViewApi().errorInvalidLocation(col, row);
            default -> getViewApi().errorCannotPlantHere(col, row);
        }
    }

    private void handlePluckPlant(String x, String y) {
        int col = parseCoord(x);
        int row = parseCoord(y);
        if (col < 0 || row < 0) {
            getViewApi().errorInvalidLocation(col, row);
            return;
        }
        if (!session.pluckPlant(col, row)) {
            getViewApi().errorNoPlantToPluck(col, row);
            return;
        }
        getViewApi().showPlantPlucked(col, row);
    }

    private void maybeReturnAfterMatch() {
        if (finishedHandled) {
            return;
        }
        MatchResult result = session.getMatchResult();
        if (result == MatchResult.WON) {
            finishedHandled = true;
            recordFinishedGame();
            user.getMiniGameProgress().markStageCompleted(
                    stage.getMiniGameId(), stage.getStageIndex());
            userDatabase.saveMiniGameProgress(user);
            parser.switchController(hubController);
        } else if (result == MatchResult.LOST) {
            finishedHandled = true;
            recordFinishedGame();
            parser.switchController(hubController);
        }
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

    public GameSession getSession() {
        return session;
    }

    public MiniGameStageConfig getStage() {
        return stage;
    }

    public ZombotanyMode getMode() {
        return mode;
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
    }

    @Override
    public void onZombieDied(String type, double x, double y) {
    }
}
