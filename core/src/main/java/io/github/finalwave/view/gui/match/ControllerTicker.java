package io.github.finalwave.view.gui.match;

import io.github.finalwave.controller.BeghouledController;
import io.github.finalwave.controller.GamePlayController;
import io.github.finalwave.controller.VaseBreakerController;
import io.github.finalwave.controller.WalnutBowlingController;
import io.github.finalwave.model.game.GameSession;


public final class ControllerTicker implements MatchTicker {
    private final GamePlayController gamePlay;
    private final VaseBreakerController vaseBreaker;
    private final WalnutBowlingController walnutBowling;
    private final BeghouledController beghouled;

    public ControllerTicker(GamePlayController gamePlay) {
        this.gamePlay = gamePlay;
        this.vaseBreaker = null;
        this.walnutBowling = null;
        this.beghouled = null;
    }

    public ControllerTicker(VaseBreakerController vaseBreaker) {
        this.gamePlay = null;
        this.vaseBreaker = vaseBreaker;
        this.walnutBowling = null;
        this.beghouled = null;
    }

    public ControllerTicker(WalnutBowlingController walnutBowling) {
        this.gamePlay = null;
        this.vaseBreaker = null;
        this.walnutBowling = walnutBowling;
        this.beghouled = null;
    }

    public ControllerTicker(BeghouledController beghouled) {
        this.gamePlay = null;
        this.vaseBreaker = null;
        this.walnutBowling = null;
        this.beghouled = beghouled;
    }

    @Override
    public void advance(int ticks) {
        if (vaseBreaker != null) {
            vaseBreaker.advance(ticks);
            return;
        }
        if (walnutBowling != null) {
            walnutBowling.advance(ticks);
            return;
        }
        if (beghouled != null) {
            beghouled.advance(ticks);
            return;
        }
        if (gamePlay != null) {
            gamePlay.advance(ticks);
        }
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
}
