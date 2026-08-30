package io.github.finalwave.view.gui.render.sync;

import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.zombie.Zombie;


public final class PotatoMineProximity {
    public static final double RECOVER_RADIUS_TILES = 4.0;

    private PotatoMineProximity() {
    }

    public static boolean inRecoverRadius(GameSession session, Plant plant) {
        if (session == null || plant == null || !plant.isAlive()) {
            return false;
        }
        int row = plant.getRow();
        int col = plant.getCol();
        for (Zombie zombie : session.getZombies()) {
            if (zombie == null || !zombie.isAlive() || zombie.getRow() != row) {
                continue;
            }
            double distance = Math.abs(zombie.getX() - col);
            if (distance <= RECOVER_RADIUS_TILES) {
                return true;
            }
        }
        return false;
    }
}
