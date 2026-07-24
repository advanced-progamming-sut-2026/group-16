package controller;

import model.command.IZombieMenuCommands;
import model.game.GameSession;
import model.game.MatchListener;
import model.game.MatchResult;
import model.game.board.PlantPlacementResult;
import model.game.entity.plant.Plant;
import model.game.entity.zombie.Zombie;
import model.item.SunType;
import model.minigame.MiniGameStageConfig;
import model.minigame.mode.IZombieMode;
import model.user.UnlockService;
import model.user.User;
import model.user.UserDatabase;
import view.api.minigame.IZombieView;

import java.util.List;
import java.util.regex.Matcher;

public class IZombieController extends ViewController implements MatchListener {

    private final User user;
    private final UserDatabase userDatabase;
    private final MiniGameHubController hubController;
    private final IZombieMode mode;
    private final GameSession session;
    private final MiniGameStageConfig stage;
    private final UnlockService unlockService = new UnlockService();
    private boolean finishedHandled;

    public IZombieController(User user,
                             UserDatabase userDatabase,
                             MiniGameHubController hubController,
                             IZombieMode mode,
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
                stage.getRedLineColumn(),
                stage.getStartingSun());
        getViewApi().showRoster(stage.getZombiePool(), stage.getZombieSunCosts());
    }

    @Override
    public void handleCommand(String input) {
        for (IZombieMenuCommands cmd : IZombieMenuCommands.values()) {
            Matcher matcher = cmd.getMatcher(input);
            if (matcher == null) {
                continue;
            }
            switch (cmd) {
                case PLACE_ZOMBIE -> handlePlaceZombie(
                        matcher.group("type"), matcher.group("x"), matcher.group("y"));
                case SHOW_ZOMBIES_ROSTER -> getViewApi().showRoster(
                        stage.getZombiePool(), stage.getZombieSunCosts());
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

    private void handlePlaceZombie(String zombieType, String x, String y) {
        int col = parseCoord(x);
        int row = parseCoord(y);
        if (col < 0 || row < 0) {
            getViewApi().errorInvalidLocation(col, row);
            return;
        }
        String type = zombieType.trim();
        PlantPlacementResult result = session.tryPlaceZombie(type, col, row);
        switch (result) {
            case SUCCESS -> getViewApi().showZombiePlaced(type, col, row);
            case BEYOND_PLANTING_LINE -> getViewApi().errorBeyondPlantingLine(
                    col, row, session.getIZombiePlacementColumn());
            case NOT_IN_LOADOUT -> getViewApi().errorNotInRoster(type);
            case INSUFFICIENT_SUN -> {
                Integer cost = stage.getZombieSunCosts().get(type);
                getViewApi().errorInsufficientSun(
                        type, cost == null ? 0 : cost, session.getSunBalance());
            }
            case UNKNOWN_PLANT -> getViewApi().errorUnknownZombie(type);
            case OUT_OF_BOUNDS -> getViewApi().errorInvalidLocation(col, row);
            default -> getViewApi().errorInvalidLocation(col, row);
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

    private IZombieView getViewApi() {
        return (IZombieView) view;
    }

    public GameSession getSession() {
        return session;
    }

    public MiniGameStageConfig getStage() {
        return stage;
    }

    public IZombieMode getMode() {
        return mode;
    }

    @Override
    public void onBrainEaten(int row) {
        getViewApi().showBrainEaten(row);
    }

    @Override
    public void onZombieSpawned(String type, int wave, int lane, int cost) {
        ZombieSeenUnlock.unlock(user, userDatabase, unlockService, type);
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
