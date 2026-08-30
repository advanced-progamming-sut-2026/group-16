package io.github.finalwave.controller;

import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.MatchListener;
import io.github.finalwave.model.game.MatchResult;
import io.github.finalwave.model.game.board.PlantPlacementResult;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.zombie.Zombie;
import io.github.finalwave.model.item.SunType;
import io.github.finalwave.model.minigame.MiniGameStageConfig;
import io.github.finalwave.model.minigame.mode.NetworkedIZombieMode;
import io.github.finalwave.model.user.User;
import io.github.finalwave.network.match.MatchEndPayload;
import io.github.finalwave.network.match.MatchRole;
import io.github.finalwave.network.match.MatchSyncService;
import io.github.finalwave.network.match.MatchWinner;
import io.github.finalwave.view.api.minigame.IZombieView;

import java.util.List;
import java.util.Map;

public final class NetworkedIZombieController extends ViewController implements MatchListener {

    private final User user;
    private final NetworkedIZombieMode mode;
    private final GameSession session;
    private final MiniGameStageConfig stage;
    private final MatchRole role;
    private final MatchSyncService matchSyncService;
    private final String opponentUsername;
    private boolean deferMatchExit;
    private boolean finishedHandled;

    public NetworkedIZombieController(User user,
                                    NetworkedIZombieMode mode,
                                    GameSession session,
                                    MiniGameStageConfig stage,
                                    MatchRole role,
                                    MatchSyncService matchSyncService,
                                    String opponentUsername) {
        this.user = user;
        this.mode = mode;
        this.session = session;
        this.stage = stage;
        this.role = role;
        this.matchSyncService = matchSyncService;
        this.opponentUsername = opponentUsername;
        this.session.setMatchListener(this);
        matchSyncService.setStateListener(this::onNetworkState);
    }

    private void onNetworkState() {
        getViewApi().showStageStarted(
                stage.getStageIndex(),
                stage.getRedLineColumn(),
                stage.getStartingSun());
        if (role == MatchRole.ZOMBIE) {
            getViewApi().showRoster(stage.getZombiePool(), stage.getZombieSunCosts());
        }
    }

    @Override
    public void displayMenu() {
        getViewApi().showStageStarted(
                stage.getStageIndex(),
                stage.getRedLineColumn(),
                stage.getStartingSun());
        if (role == MatchRole.ZOMBIE) {
            getViewApi().showRoster(stage.getZombiePool(), stage.getZombieSunCosts());
        } else {
            getViewApi().showRoster(List.of(), Map.of());
        }
    }

    public MatchRole role() {
        return role;
    }

    public String opponentUsername() {
        return opponentUsername;
    }

    public GameSession session() {
        return session;
    }

    public User getUser() {
        return user;
    }

    public MiniGameStageConfig getStage() {
        return stage;
    }

    public void setDeferMatchExit(boolean deferMatchExit) {
        this.deferMatchExit = deferMatchExit;
    }

    public void confirmMatchExit() {
        matchSyncService.setStateListener(null);
        matchSyncService.clear();
        navigator.pop();
    }

    public void restartMatch() {
        matchSyncService.setStateListener(null);
        matchSyncService.clear();
        navigator.pop();
    }

    public void advance(int ticks) {
        if (role != MatchRole.PLANT || ticks <= 0) {
            return;
        }
        session.advanceTicks(ticks);
        matchSyncService.tickHost();
        maybeReturnAfterMatch();
    }

    public void advanceGuest(int ticks) {
        if (role != MatchRole.ZOMBIE || ticks <= 0) {
            return;
        }
        session.advanceGuestDisplayTicks(ticks);
    }

    public void tickHostSync() {
        if (role == MatchRole.PLANT) {
            matchSyncService.tickHost();
            maybeReturnAfterMatch();
        }
    }

    public PlantPlacementResult plantSeed(String plantName, int col, int row) {
        if (role != MatchRole.PLANT) {
            return PlantPlacementResult.TILE_BLOCKED;
        }
        PlantPlacementResult result = session.tryPlant(plantName, col, row, 1);
        if (result == PlantPlacementResult.SUCCESS) {
            tickHostSync();
        }
        return result;
    }

    public PlantPlacementResult placeZombie(String alias, int col, int row) {
        if (role != MatchRole.ZOMBIE) {
            return PlantPlacementResult.TILE_BLOCKED;
        }
        PlantPlacementResult result = session.tryPlaceZombie(alias, col, row);
        if (result == PlantPlacementResult.SUCCESS) {
            matchSyncService.sendGuestPlaceZombie(alias, col, row);
        }
        return result;
    }

    public boolean collectSunAt(int col, int row) {
        return session.collectSunAt(col, row);
    }

    public void cheatAddSun(int amount) {
        if (amount > 0) {
            session.addSunBalance(amount);
        }
    }

    public void handleNetworkMatchEnd(MatchEndPayload payload) {
        if (payload == null) {
            return;
        }
        if (payload.getWinner() == MatchWinner.PLANT) {
            if (role == MatchRole.PLANT) {
                session.winMatch();
            } else {
                session.loseMatch();
            }
        } else if (role == MatchRole.ZOMBIE) {
            session.winMatch();
        } else {
            session.loseMatch();
        }
        maybeReturnAfterMatch();
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
        if (result == MatchResult.WON) {
            getViewApi().showWinMessage();
        } else {
            getViewApi().showLoseMessage();
        }
        if (deferMatchExit) {
            return;
        }
        matchSyncService.clear();
        navigator.pop();
    }

    private IZombieView getViewApi() {
        return (IZombieView) view;
    }

    @Override
    public void onBrainEaten(int row) {
        getViewApi().showBrainEaten(row);
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
