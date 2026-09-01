package io.github.finalwave.view.gui.match;

import io.github.finalwave.controller.BeghouledController;
import io.github.finalwave.controller.CouchIZombieController;
import io.github.finalwave.controller.GamePlayController;
import io.github.finalwave.controller.IZombieController;
import io.github.finalwave.controller.NetworkedIZombieController;
import io.github.finalwave.controller.VaseBreakerController;
import io.github.finalwave.controller.WalnutBowlingController;
import io.github.finalwave.controller.ZombotanyController;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.network.match.MatchRole;


public final class ControllerTicker implements MatchTicker {
    private final GamePlayController gamePlay;
    private final VaseBreakerController vaseBreaker;
    private final WalnutBowlingController walnutBowling;
    private final IZombieController iZombie;
    private final NetworkedIZombieController networkedIZombie;
    private final CouchIZombieController couchIZombie;
    private final BeghouledController beghouled;
    private final ZombotanyController zombotany;

    public ControllerTicker(GamePlayController gamePlay) {
        this.gamePlay = gamePlay;
        this.vaseBreaker = null;
        this.walnutBowling = null;
        this.iZombie = null;
        this.networkedIZombie = null;
        this.couchIZombie = null;
        this.beghouled = null;
        this.zombotany = null;
    }

    public ControllerTicker(VaseBreakerController vaseBreaker) {
        this.gamePlay = null;
        this.vaseBreaker = vaseBreaker;
        this.walnutBowling = null;
        this.iZombie = null;
        this.networkedIZombie = null;
        this.couchIZombie = null;
        this.beghouled = null;
        this.zombotany = null;
    }

    public ControllerTicker(WalnutBowlingController walnutBowling) {
        this.gamePlay = null;
        this.vaseBreaker = null;
        this.walnutBowling = walnutBowling;
        this.iZombie = null;
        this.networkedIZombie = null;
        this.couchIZombie = null;
        this.beghouled = null;
        this.zombotany = null;
    }

    public ControllerTicker(IZombieController iZombie) {
        this.gamePlay = null;
        this.vaseBreaker = null;
        this.walnutBowling = null;
        this.iZombie = iZombie;
        this.networkedIZombie = null;
        this.couchIZombie = null;
        this.beghouled = null;
        this.zombotany = null;
    }

    public ControllerTicker(NetworkedIZombieController networkedIZombie) {
        this.gamePlay = null;
        this.vaseBreaker = null;
        this.walnutBowling = null;
        this.iZombie = null;
        this.networkedIZombie = networkedIZombie;
        this.couchIZombie = null;
        this.beghouled = null;
        this.zombotany = null;
    }

    public ControllerTicker(CouchIZombieController couchIZombie) {
        this.gamePlay = null;
        this.vaseBreaker = null;
        this.walnutBowling = null;
        this.iZombie = null;
        this.networkedIZombie = null;
        this.couchIZombie = couchIZombie;
        this.beghouled = null;
        this.zombotany = null;
    }

    public ControllerTicker(BeghouledController beghouled) {
        this.gamePlay = null;
        this.vaseBreaker = null;
        this.walnutBowling = null;
        this.iZombie = null;
        this.networkedIZombie = null;
        this.couchIZombie = null;
        this.beghouled = beghouled;
        this.zombotany = null;
    }

    public ControllerTicker(ZombotanyController zombotany) {
        this.gamePlay = null;
        this.vaseBreaker = null;
        this.walnutBowling = null;
        this.iZombie = null;
        this.networkedIZombie = null;
        this.couchIZombie = null;
        this.beghouled = null;
        this.zombotany = zombotany;
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
        if (networkedIZombie != null) {
            if (networkedIZombie.role() == MatchRole.PLANT) {
                networkedIZombie.advance(ticks);
            } else {
                networkedIZombie.advanceGuest(ticks);
            }
            return;
        }
        if (couchIZombie != null) {
            couchIZombie.advance(ticks);
            return;
        }
        if (iZombie != null) {
            iZombie.advance(ticks);
            return;
        }
        if (beghouled != null) {
            beghouled.advance(ticks);
            return;
        }
        if (zombotany != null) {
            zombotany.advance(ticks);
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
        if (iZombie != null) {
            return iZombie.session();
        }
        if (networkedIZombie != null) {
            return networkedIZombie.session();
        }
        if (couchIZombie != null) {
            return couchIZombie.session();
        }
        if (beghouled != null) {
            return beghouled.session();
        }
        if (zombotany != null) {
            return zombotany.session();
        }
        return gamePlay == null ? null : gamePlay.session();
    }
}
