package io.github.finalwave.controller;

import io.github.finalwave.model.command.WalnutBowlingMenuCommands;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.MatchListener;
import io.github.finalwave.model.game.MatchResult;
import io.github.finalwave.model.game.board.PlantPlacementResult;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.zombie.Zombie;
import io.github.finalwave.model.item.SunType;
import io.github.finalwave.model.minigame.MiniGameStageConfig;
import io.github.finalwave.model.minigame.bowling.BowlingNutType;
import io.github.finalwave.model.minigame.mode.WalnutBowlingMode;
import io.github.finalwave.model.user.UnlockService;
import io.github.finalwave.model.user.User;
import io.github.finalwave.model.user.UserDatabase;
import io.github.finalwave.view.api.minigame.WalnutBowlingView;

import java.util.List;
import java.util.regex.Matcher;

public class WalnutBowlingController extends ViewController implements MatchListener {

    private final User user;
    private final UserDatabase userDatabase;
    private final WalnutBowlingMode mode;
    private final GameSession session;
    private final MiniGameStageConfig stage;
    private final UnlockService unlockService = new UnlockService();
    private boolean finishedHandled;
    private boolean deferMatchExit;

    public WalnutBowlingController(User user,
                                   UserDatabase userDatabase,
                                   WalnutBowlingMode mode,
                                   GameSession session,
                                   MiniGameStageConfig stage) {
        this.user = user;
        this.userDatabase = userDatabase;
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
                    navigator.pop();
                    return;
                }
            }
            maybeReturnAfterMatch();
            return;
        }
        getViewApi().errorInvalidCommand();
    }

    private void handlePlantNut(String plantType, String x, String y) {
        plantSeed(plantType, parseCoord(x), parseCoord(y));
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

    private WalnutBowlingView getViewApi() {
        return (WalnutBowlingView) view;
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

    public WalnutBowlingMode getMode() {
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

    public PlantPlacementResult plantSeed(String plantName, int col, int row) {
        if (plantName == null || plantName.isBlank()) {
            getViewApi().errorUnknownPlant(plantName);
            return PlantPlacementResult.UNKNOWN_PLANT;
        }
        if (col < 0 || row < 0) {
            getViewApi().errorInvalidLocation(col, row);
            return PlantPlacementResult.OUT_OF_BOUNDS;
        }
        PlantPlacementResult result = session.tryPlantBowlingNut(plantName.trim(), col, row);
        switch (result) {
            case SUCCESS -> {
            }
            case BEYOND_PLANTING_LINE -> getViewApi().errorBeyondPlantingLine(
                    col, row, session.getWalnutBowlingRedLineColumn());
            case NOT_ON_CONVEYOR_BELT -> getViewApi().errorPlantNotOnConveyorBelt(plantName);
            case UNKNOWN_PLANT -> getViewApi().errorUnknownPlant(plantName);
            case OUT_OF_BOUNDS -> getViewApi().errorInvalidLocation(col, row);
            default -> getViewApi().errorCannotPlantHere(col, row);
        }
        maybeReturnAfterMatch();
        return result;
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
