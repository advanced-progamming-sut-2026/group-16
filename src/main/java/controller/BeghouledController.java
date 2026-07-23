package controller;

import model.command.BeghouledMenuCommands;
import model.game.GameSession;
import model.game.MatchListener;
import model.game.MatchResult;
import model.game.entity.plant.Plant;
import model.game.entity.zombie.Zombie;
import model.item.SunType;
import model.minigame.MiniGameStageConfig;
import model.minigame.beghouled.BeghouledSwapResult;
import model.minigame.beghouled.BeghouledUpgradeResult;
import model.minigame.mode.BeghouledMode;
import model.user.User;
import model.user.UserDatabase;
import view.api.minigame.BeghouledView;

import java.util.List;
import java.util.regex.Matcher;

public class BeghouledController extends ViewController implements MatchListener {

    private final User user;
    private final UserDatabase userDatabase;
    private final MiniGameHubController hubController;
    private final BeghouledMode mode;
    private final GameSession session;
    private final MiniGameStageConfig stage;
    private boolean finishedHandled;

    public BeghouledController(User user,
                               UserDatabase userDatabase,
                               MiniGameHubController hubController,
                               BeghouledMode mode,
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
                case SWAP_PLANTS -> handleSwap(
                        matcher.group("ax"), matcher.group("ay"),
                        matcher.group("bx"), matcher.group("by"));
                case SHOW_UPGRADES -> getViewApi().showUpgrades(stage.getUpgrades());
                case UPGRADE_PLANT -> handleUpgrade(matcher.group("type"));
                case ADVANCE_TIME -> handleAdvanceTime(matcher.group("count"));
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

    private void handleSwap(String ax, String ay, String bx, String by) {
        int colA = parseCoord(ax);
        int rowA = parseCoord(ay);
        int colB = parseCoord(bx);
        int rowB = parseCoord(by);
        if (colA < 0 || rowA < 0 || colB < 0 || rowB < 0) {
            getViewApi().errorSwapOutOfBounds();
            return;
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
    }

    private void handleUpgrade(String plantName) {
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

    private BeghouledView getViewApi() {
        return (BeghouledView) view;
    }

    public GameSession getSession() {
        return session;
    }

    public MiniGameStageConfig getStage() {
        return stage;
    }

    public BeghouledMode getMode() {
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
