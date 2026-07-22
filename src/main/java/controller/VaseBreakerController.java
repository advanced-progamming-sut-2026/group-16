package controller;

import model.command.VaseBreakerMenuCommands;
import model.game.GameSession;
import model.game.MatchListener;
import model.game.MatchResult;
import model.game.board.PlantPlacementResult;
import model.game.entity.Vase;
import model.game.entity.plant.Plant;
import model.game.entity.zombie.Zombie;
import model.item.SunType;
import model.minigame.MiniGameStageConfig;
import model.minigame.mode.VaseBreakerMode;
import model.user.User;
import model.user.UserDatabase;
import view.api.minigame.VaseBreakerView;

import java.util.List;
import java.util.regex.Matcher;

public class VaseBreakerController extends ViewController implements MatchListener {

    private final User user;
    private final UserDatabase userDatabase;
    private final MiniGameHubController hubController;
    private final VaseBreakerMode mode;
    private final GameSession session;
    private final MiniGameStageConfig stage;
    private boolean finishedHandled;

    public VaseBreakerController(User user,
                                 UserDatabase userDatabase,
                                 MiniGameHubController hubController,
                                 VaseBreakerMode mode,
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
        getViewApi().showStageStarted(stage.getStageIndex());
    }

    @Override
    public void handleCommand(String input) {
        for (VaseBreakerMenuCommands cmd : VaseBreakerMenuCommands.values()) {
            Matcher matcher = cmd.getMatcher(input);
            if (matcher == null) {
                continue;
            }
            switch (cmd) {
                case SMASH_VASE -> handleSmashVase(matcher.group("x"), matcher.group("y"));
                case PLANT_SEED -> handlePlantSeed(matcher.group("x"), matcher.group("y"));
                case ADVANCE_TIME -> handleAdvanceTime(matcher.group("count"));
                case SHOW_MAP -> getViewApi().showMap(session.renderMap());
                case ZOMBIES_INFO -> getViewApi().showZombiesInfo(session.getZombies());
                case RELEASE_THE_NUKE -> {
                    session.nukeAllZombies();
                    getViewApi().showNukeActivated();
                    if (session.getActiveMiniGameHandler() != null) {
                        session.getActiveMiniGameHandler().onTick(session);
                    }
                }
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

    private void handleSmashVase(String x, String y) {
        int col = parseCoord(x);
        int row = parseCoord(y);
        if (col < 0 || row < 0) {
            getViewApi().errorInvalidLocation(col, row);
            return;
        }
        if (!session.smashVase(col, row)) {
            getViewApi().errorNoVaseAt(col, row);
        }
    }

    private void handlePlantSeed(String x, String y) {
        int col = parseCoord(x);
        int row = parseCoord(y);
        if (col < 0 || row < 0) {
            getViewApi().errorInvalidLocation(col, row);
            return;
        }
        PlantPlacementResult result = session.plantFromSeedPacket(col, row);
        switch (result) {
            case SUCCESS -> {
            }
            case NO_SEED_PACKET -> getViewApi().errorNoSeedPacketAt(col, row);
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

    private VaseBreakerView getViewApi() {
        return (VaseBreakerView) view;
    }

    public GameSession getSession() {
        return session;
    }

    public MiniGameStageConfig getStage() {
        return stage;
    }

    public VaseBreakerMode getMode() {
        return mode;
    }

    @Override
    public void onVaseSmashed(int col, int row, Vase.Content content) {
        getViewApi().showVaseSmashed(col, row, content);
    }

    @Override
    public void onSeedPacketDropped(String plantName, int col, int row) {
        getViewApi().showSeedPacketDropped(plantName, col, row);
    }

    @Override
    public void onSeedPacketExpired(String plantName, int col, int row) {
        getViewApi().showSeedPacketExpired(plantName, col, row);
    }

    @Override
    public void onSeedPacketPlanted(String plantName, int col, int row) {
        getViewApi().showSeedPacketPlanted(plantName, col, row);
    }

    @Override
    public void onZombieSpawned(String type, int wave, int lane, int cost) {
        getViewApi().showZombieSpawned(type, lane, lane);
    }

    @Override
    public void onZombieDied(String type, double x, double y) {
        getViewApi().showZombieDied(type, x, y);
    }

    @Override
    public void onWin() {
        if (session.getActiveMiniGameHandler() != null) {
            session.getActiveMiniGameHandler().onLevelWon(session);
        }
        getViewApi().showWinMessage();
    }

    @Override
    public void onLose() {
        if (session.getActiveMiniGameHandler() != null) {
            session.getActiveMiniGameHandler().onLevelLost(session);
        }
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
