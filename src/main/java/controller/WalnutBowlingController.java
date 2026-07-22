package controller;

import model.command.WalnutBowlingMenuCommands;
import model.game.GameSession;
import model.game.MatchListener;
import model.game.MatchResult;
import model.game.board.PlantPlacementResult;
import model.game.entity.plant.Plant;
import model.game.entity.zombie.Zombie;
import model.item.SunType;
import model.minigame.MiniGameStageConfig;
import model.minigame.bowling.BowlingNutType;
import model.minigame.mode.WalnutBowlingMode;
import model.user.User;
import model.user.UserDatabase;
import view.api.minigame.WalnutBowlingView;

import java.util.List;
import java.util.regex.Matcher;

public class WalnutBowlingController extends ViewController implements MatchListener {

    private final User user;
    private final UserDatabase userDatabase;
    private final MiniGameHubController hubController;
    private final WalnutBowlingMode mode;
    private final GameSession session;
    private final MiniGameStageConfig stage;
    private boolean finishedHandled;

    public WalnutBowlingController(User user,
                                   UserDatabase userDatabase,
                                   MiniGameHubController hubController,
                                   WalnutBowlingMode mode,
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
        getViewApi().showStageStarted(stage.getStageIndex(), stage.getRedLineColumn());
        getViewApi().showConveyorBelt(session.getConveyorBeltPlants());
    }

    @Override
    public void handleCommand(String input) {
        for (WalnutBowlingMenuCommands cmd : WalnutBowlingMenuCommands.values()) {
            Matcher matcher = cmd.getMatcher(input);
            if (matcher == null) {
                continue;
            }
            switch (cmd) {
                case PLANT_PLANT -> handlePlantNut(
                        matcher.group("type"), matcher.group("x"), matcher.group("y"));
                case SHOW_CONVEYOR_BELT -> getViewApi().showConveyorBelt(session.getConveyorBeltPlants());
                case ADVANCE_TIME -> handleAdvanceTime(matcher.group("count"));
                case SHOW_MAP -> getViewApi().showMap(session.renderMap());
                case ZOMBIES_INFO -> getViewApi().showZombiesInfo(session.getZombies());
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

    private void handlePlantNut(String plantType, String x, String y) {
        int col = parseCoord(x);
        int row = parseCoord(y);
        if (col < 0 || row < 0) {
            getViewApi().errorInvalidLocation(col, row);
            return;
        }
        PlantPlacementResult result = session.tryPlantBowlingNut(plantType.trim(), col, row);
        switch (result) {
            case SUCCESS -> {
            }
            case BEYOND_PLANTING_LINE -> getViewApi().errorBeyondPlantingLine(
                    col, row, session.getWalnutBowlingRedLineColumn());
            case NOT_ON_CONVEYOR_BELT -> getViewApi().errorPlantNotOnConveyorBelt(plantType);
            case UNKNOWN_PLANT -> getViewApi().errorUnknownPlant(plantType);
            case OUT_OF_BOUNDS -> getViewApi().errorInvalidLocation(col, row);
            default -> getViewApi().errorCannotPlantHere(col, row);
        }
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

    private void maybeReturnAfterMatch() {
        if (finishedHandled) {
            return;
        }
        MatchResult result = session.getMatchResult();
        if (result == MatchResult.WON) {
            finishedHandled = true;
            user.getMiniGameProgress().markStageCompleted(
                    stage.getMiniGameId(), stage.getStageIndex());
            userDatabase.saveMiniGameProgress(user);
            parser.switchController(hubController);
        } else if (result == MatchResult.LOST) {
            finishedHandled = true;
            parser.switchController(hubController);
        }
    }

    private int parseCoord(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private WalnutBowlingView getViewApi() {
        return (WalnutBowlingView) view;
    }

    public GameSession getSession() {
        return session;
    }

    public MiniGameStageConfig getStage() {
        return stage;
    }

    public WalnutBowlingMode getMode() {
        return mode;
    }

    @Override
    public void onConveyorBeltPlantArrived(String plantName) {
        getViewApi().showConveyorBeltPlantArrived(plantName);
    }

    @Override
    public void onBowlingNutSpawned(String plantName, int col, int row) {
        getViewApi().showBowlingNutSpawned(plantName, col, row);
    }

    @Override
    public void onBowlingNutHit(BowlingNutType type, String zombieType, double x, double row) {
        getViewApi().showBowlingNutHit(type, zombieType, x, row);
    }

    @Override
    public void onBowlingNutExploded(int col, int row) {
        getViewApi().showBowlingNutExploded(col, row);
    }

    @Override
    public void onZombieSpawned(String type, int wave, int lane, int cost) {
    }

    @Override
    public void onZombieDied(String type, double x, double y) {
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
}
