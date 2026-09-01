package io.github.finalwave.view.gui.input;

import io.github.finalwave.controller.BeghouledController;
import io.github.finalwave.controller.CouchIZombieController;
import io.github.finalwave.controller.GamePlayController;
import io.github.finalwave.controller.IZombieController;
import io.github.finalwave.controller.NetworkedIZombieController;
import io.github.finalwave.controller.VaseBreakerController;
import io.github.finalwave.controller.WalnutBowlingController;
import io.github.finalwave.controller.ZombotanyController;
import io.github.finalwave.model.game.GameSession;


public final class ControllerLawnHost implements LawnActionHost {
    private final GamePlayController gamePlay;
    private final VaseBreakerController vaseBreaker;
    private final WalnutBowlingController walnutBowling;
    private final IZombieController iZombie;
    private final NetworkedIZombieController networkedIZombie;
    private final CouchIZombieController couchIZombie;
    private final BeghouledController beghouled;
    private final ZombotanyController zombotany;

    public ControllerLawnHost(GamePlayController gamePlay) {
        this.gamePlay = gamePlay;
        this.vaseBreaker = null;
        this.walnutBowling = null;
        this.iZombie = null;
        this.networkedIZombie = null;
        this.couchIZombie = null;
        this.beghouled = null;
        this.zombotany = null;
    }

    public ControllerLawnHost(VaseBreakerController vaseBreaker) {
        this.gamePlay = null;
        this.vaseBreaker = vaseBreaker;
        this.walnutBowling = null;
        this.iZombie = null;
        this.networkedIZombie = null;
        this.couchIZombie = null;
        this.beghouled = null;
        this.zombotany = null;
    }

    public ControllerLawnHost(WalnutBowlingController walnutBowling) {
        this.gamePlay = null;
        this.vaseBreaker = null;
        this.walnutBowling = walnutBowling;
        this.iZombie = null;
        this.networkedIZombie = null;
        this.couchIZombie = null;
        this.beghouled = null;
        this.zombotany = null;
    }

    public ControllerLawnHost(IZombieController iZombie) {
        this.gamePlay = null;
        this.vaseBreaker = null;
        this.walnutBowling = null;
        this.iZombie = iZombie;
        this.networkedIZombie = null;
        this.couchIZombie = null;
        this.beghouled = null;
        this.zombotany = null;
    }

    public ControllerLawnHost(NetworkedIZombieController networkedIZombie) {
        this.gamePlay = null;
        this.vaseBreaker = null;
        this.walnutBowling = null;
        this.iZombie = null;
        this.networkedIZombie = networkedIZombie;
        this.couchIZombie = null;
        this.beghouled = null;
        this.zombotany = null;
    }

    public ControllerLawnHost(CouchIZombieController couchIZombie) {
        this.gamePlay = null;
        this.vaseBreaker = null;
        this.walnutBowling = null;
        this.iZombie = null;
        this.networkedIZombie = null;
        this.couchIZombie = couchIZombie;
        this.beghouled = null;
        this.zombotany = null;
    }

    public ControllerLawnHost(BeghouledController beghouled) {
        this.gamePlay = null;
        this.vaseBreaker = null;
        this.walnutBowling = null;
        this.iZombie = null;
        this.networkedIZombie = null;
        this.couchIZombie = null;
        this.beghouled = beghouled;
        this.zombotany = null;
    }

    public ControllerLawnHost(ZombotanyController zombotany) {
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
        if (networkedIZombie != null) {
            networkedIZombie.plantSeed(plantName, col, row);
            return;
        }
        if (couchIZombie != null) {
            couchIZombie.plantSeed(plantName, col, row);
            return;
        }
        if (iZombie != null || beghouled != null) {
            return;
        }
        if (zombotany != null) {
            zombotany.plantAt(plantName, col, row);
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
        if (iZombie != null) {
            return iZombie.collectSunAt(col, row);
        }
        if (networkedIZombie != null) {
            return networkedIZombie.collectSunAt(col, row);
        }
        if (couchIZombie != null) {
            return couchIZombie.collectSunAt(col, row);
        }
        if (beghouled != null) {
            return beghouled.collectSunAt(col, row);
        }
        if (zombotany != null) {
            return zombotany.collectSunAt(col, row);
        }
        return gamePlay != null && gamePlay.collectSunAt(col, row);
    }

    @Override
    public boolean shovelAt(int col, int row) {
        if (vaseBreaker != null) {
            return vaseBreaker.shovelAt(col, row);
        }
        if (walnutBowling != null || iZombie != null || beghouled != null) {
            return false;
        }
        if (networkedIZombie != null) {
            return networkedIZombie.shovelAt(col, row);
        }
        if (couchIZombie != null) {
            return couchIZombie.shovelAt(col, row);
        }
        if (zombotany != null) {
            return zombotany.shovelAt(col, row);
        }
        return gamePlay != null && gamePlay.shovelAt(col, row);
    }

    @Override
    public boolean feedAt(int col, int row) {
        if (vaseBreaker != null) {
            return vaseBreaker.feedAt(col, row);
        }
        if (walnutBowling != null || iZombie != null || beghouled != null) {
            return false;
        }
        if (zombotany != null) {
            return zombotany.feedAt(col, row);
        }
        return gamePlay != null && gamePlay.feedAt(col, row);
    }

    @Override
    public void placeZombie(String alias, int col, int row) {
        if (iZombie != null) {
            iZombie.placeZombie(alias, col, row);
            return;
        }
        if (networkedIZombie != null) {
            networkedIZombie.placeZombie(alias, col, row);
            return;
        }
        if (couchIZombie != null) {
            couchIZombie.placeZombie(alias, col, row);
        }
    }
}
