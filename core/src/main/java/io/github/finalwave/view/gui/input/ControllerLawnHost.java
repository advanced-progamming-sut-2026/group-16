package io.github.finalwave.view.gui.input;

import io.github.finalwave.controller.GamePlayController;
import io.github.finalwave.controller.VaseBreakerController;
import io.github.finalwave.controller.WalnutBowlingController;
import io.github.finalwave.model.game.GameSession;


public final class ControllerLawnHost implements LawnActionHost {
    private final GamePlayController gamePlay;
    private final VaseBreakerController vaseBreaker;
    private final WalnutBowlingController walnutBowling;

    public ControllerLawnHost(GamePlayController gamePlay) {
        this.gamePlay = gamePlay;
        this.vaseBreaker = null;
        this.walnutBowling = null;
    }

    public ControllerLawnHost(VaseBreakerController vaseBreaker) {
        this.gamePlay = null;
        this.vaseBreaker = vaseBreaker;
        this.walnutBowling = null;
    }

    public ControllerLawnHost(WalnutBowlingController walnutBowling) {
        this.gamePlay = null;
        this.vaseBreaker = null;
        this.walnutBowling = walnutBowling;
    }

    @Override
    public GameSession session() {
        if (vaseBreaker != null) {
            return vaseBreaker.session();
        }
        if (walnutBowling != null) {
            return walnutBowling.session();
        }
        return gamePlay == null ? null : gamePlay.session();
    }

    @Override
    public void plantSeed(String plantName, int col, int row) {
        if (vaseBreaker != null) {
            vaseBreaker.plantSeed(plantName, col, row);
            return;
        }
        if (walnutBowling != null) {
            walnutBowling.plantSeed(plantName, col, row);
            return;
        }
        if (gamePlay != null) {
            gamePlay.plantAt(plantName, col, row);
        }
    }

    @Override
    public boolean smashVase(int col, int row) {
        if (vaseBreaker != null) {
            return vaseBreaker.smashVase(col, row);
        }
        return false;
    }

    @Override
    public boolean collectSunAt(int col, int row) {
        if (vaseBreaker != null) {
            return vaseBreaker.collectSunAt(col, row);
        }
        if (walnutBowling != null) {
            return false;
        }
        return gamePlay != null && gamePlay.collectSunAt(col, row);
    }

    @Override
    public boolean shovelAt(int col, int row) {
        if (vaseBreaker != null) {
            return vaseBreaker.shovelAt(col, row);
        }
        if (walnutBowling != null) {
            return false;
        }
        return gamePlay != null && gamePlay.shovelAt(col, row);
    }

    @Override
    public boolean feedAt(int col, int row) {
        if (vaseBreaker != null) {
            return vaseBreaker.feedAt(col, row);
        }
        if (walnutBowling != null) {
            return false;
        }
        return gamePlay != null && gamePlay.feedAt(col, row);
    }
}
