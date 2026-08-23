package io.github.finalwave.controller;

import io.github.finalwave.model.command.BeghouledMenuCommands;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.MatchListener;
import io.github.finalwave.model.game.MatchResult;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.zombie.Zombie;
import io.github.finalwave.model.item.SunType;
import io.github.finalwave.model.minigame.MiniGameStageConfig;
import io.github.finalwave.model.minigame.beghouled.BeghouledSwapOutcome;
import io.github.finalwave.model.minigame.beghouled.BeghouledSwapResult;
import io.github.finalwave.model.minigame.beghouled.BeghouledUpgradeOutcome;
import io.github.finalwave.model.minigame.beghouled.BeghouledUpgradeResult;
import io.github.finalwave.model.minigame.mode.BeghouledMode;
import io.github.finalwave.model.user.UnlockService;
import io.github.finalwave.model.user.User;
import io.github.finalwave.model.user.UserDatabase;
import io.github.finalwave.view.api.minigame.BeghouledView;

import java.util.List;
import java.util.regex.Matcher;

public class BeghouledController extends ViewController implements MatchListener {

    private final User user;
    private final UserDatabase userDatabase;
    private final BeghouledMode mode;
    private final GameSession session;
    private final MiniGameStageConfig stage;
    private final UnlockService unlockService = new UnlockService();
    private boolean finishedHandled;
    private boolean deferMatchExit;

    public BeghouledController(User user,
                               UserDatabase userDatabase,
                               BeghouledMode mode,
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
        getViewApi().showStageStarted(
                stage.getStageIndex(),
                stage.getMatchTarget(),
                stage.getPlantSeedPool());
        getViewApi().showUpgrades(stage.getUpgrades());
    }

    @Override
    public void handleCommand(String input) {
        for (BeghouledMenuCommands cmd : BeghouledMenuCommands.values()) {
            Matcher matcher = cmd.getMatcher(input);
            if (matcher == null) {
                continue;
            }
            switch (cmd) {
                case SWAP_PLANTS -> swapPlants(
                        parseCoord(matcher.group("ax")), parseCoord(matcher.group("ay")),
                        parseCoord(matcher.group("bx")), parseCoord(matcher.group("by")));
                case SHOW_UPGRADES -> getViewApi().showUpgrades(stage.getUpgrades());
                case UPGRADE_PLANT -> upgradePlant(matcher.group("type"));
                case ADVANCE_TIME -> handleAdvanceTime(matcher.group("count"));
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

    public BeghouledSwapResult swapPlants(int colA, int rowA, int colB, int rowB) {
        if (colA < 0 || rowA < 0 || colB < 0 || rowB < 0) {
            getViewApi().errorSwapOutOfBounds();
            return BeghouledSwapResult.failure(BeghouledSwapOutcome.OUT_OF_BOUNDS);
        }
        BeghouledSwapResult result = session.trySwapBeghouledPlants(colA, rowA, colB, rowB);
        switch (result.outcome()) {
            case SUCCESS -> {
                getViewApi().showSwapAccepted(result.matchesCleared(), result.sunAwarded());
                if (result.boardReset()) {
                    getViewApi().showBoardReset();
                }
            }
            case OUT_OF_BOUNDS -> getViewApi().errorSwapOutOfBounds();
            case NOT_ADJACENT -> getViewApi().errorSwapNotAdjacent();
            case NO_MATCH_FORMED -> getViewApi().errorSwapNoMatch();
            case MISSING_PLANT -> getViewApi().errorSwapMissingPlant();
            case CRATER_BLOCKED -> getViewApi().errorSwapCraterBlocked();
        }
        maybeReturnAfterMatch();
        return result;
    }

    public BeghouledUpgradeResult upgradePlant(String plantName) {
        if (plantName == null || plantName.isBlank()) {
            getViewApi().errorUpgradeUnknown(plantName);
            return BeghouledUpgradeResult.failure(BeghouledUpgradeOutcome.UNKNOWN_UPGRADE);
        }
        String type = plantName.trim();
        BeghouledUpgradeResult result = session.tryBeghouledUpgrade(type);
        switch (result.outcome()) {
            case SUCCESS -> {
                var rule = session.getBeghouledBoard().getUpgradeCatalog().findRule(type).orElse(null);
                String to = rule == null ? "?" : rule.toPlant();
                getViewApi().showUpgradeApplied(type, to, result.plantsConverted(), result.sunSpent());
            }
            case UNKNOWN_UPGRADE -> getViewApi().errorUpgradeUnknown(type);
            case INSUFFICIENT_SUN -> {
                int cost = session.getBeghouledBoard().getUpgradeCatalog()
                        .findRule(type).map(r -> r.sunCost()).orElse(0);
                getViewApi().errorUpgradeInsufficientSun(cost, session.getSunBalance());
            }
            case NO_PLANTS_OF_TYPE -> getViewApi().errorUpgradeNoPlants(type);
        }
        maybeReturnAfterMatch();
        return result;
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

    private BeghouledView getViewApi() {
        return (BeghouledView) view;
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

    public BeghouledMode getMode() {
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

    public boolean collectSunAt(int col, int row) {
        if (col < 0 || row < 0 || session == null) {
            return false;
        }
        return session.collectSunAt(col, row);
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
