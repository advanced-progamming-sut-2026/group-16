package io.github.finalwave.view.gui.input;

import io.github.finalwave.controller.BeghouledController;
import io.github.finalwave.controller.GamePlayController;
import io.github.finalwave.controller.VaseBreakerController;
import io.github.finalwave.controller.WalnutBowlingController;
import io.github.finalwave.model.game.GameSession;


public final class ControllerLawnHost implements LawnActionHost {
    private final GamePlayController gamePlay;
    private final VaseBreakerController vaseBreaker;
    private final WalnutBowlingController walnutBowling;
    private final BeghouledController beghouled;

    public ControllerLawnHost(GamePlayController gamePlay) {
        this.gamePlay = gamePlay;
        this.vaseBreaker = null;
        this.walnutBowling = null;
        this.beghouled = null;
    }

    public ControllerLawnHost(VaseBreakerController vaseBreaker) {
        this.gamePlay = null;
        this.vaseBreaker = vaseBreaker;
        this.walnutBowling = null;
        this.beghouled = null;
    }

    public ControllerLawnHost(WalnutBowlingController walnutBowling) {
        this.gamePlay = null;
        this.vaseBreaker = null;
        this.walnutBowling = walnutBowling;
        this.beghouled = null;
    }

    public ControllerLawnHost(BeghouledController beghouled) {
        this.gamePlay = null;
        this.vaseBreaker = null;
        this.walnutBowling = null;
        this.beghouled = beghouled;
    }

    @Override
    public GameSession session() {
        if (vaseBreaker != null) {
            return vaseBreaker.session();
        }
        if (walnutBowling != null) {
            return walnutBowling.session();
        }
        if (beghouled != null) {
            return beghouled.session();
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
        if (beghouled != null) {
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
        if (beghouled != null) {
            return beghouled.collectSunAt(col, row);
        }
        return gamePlay != null && gamePlay.collectSunAt(col, row);
    }

    @Override
    public boolean shovelAt(int col, int row) {
        if (vaseBreaker != null) {
            return vaseBreaker.shovelAt(col, row);
        }
        if (walnutBowling != null || beghouled != null) {
            return false;
        }
        return gamePlay != null && gamePlay.shovelAt(col, row);
    }

    @Override
    public boolean feedAt(int col, int row) {
        if (vaseBreaker != null) {
            return vaseBreaker.feedAt(col, row);
        }
        if (walnutBowling != null || beghouled != null) {
            return false;
        }
        return gamePlay != null && gamePlay.feedAt(col, row);
    }
}
