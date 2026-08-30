package io.github.finalwave.model.game.entity.plant.food;

import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.projectile.ProjectileProfile;


public final class FumeShroomPlantFood {
    public static final int LOOP_TICKS = 53;
    public static final int WINDUP_TICKS = 16;

    private PeaPodPlantFood.Phase phase = PeaPodPlantFood.Phase.NONE;
    private int ticksRemaining;
    private int windupRemaining;
    private boolean fired;

    public boolean start(Plant plant, GameContext context) {
        if (phase != PeaPodPlantFood.Phase.NONE) {
            return false;
        }
        phase = PeaPodPlantFood.Phase.LOOP;
        ticksRemaining = LOOP_TICKS;
        windupRemaining = WINDUP_TICKS;
        fired = false;
        return true;
    }

    public void tick(Plant plant, GameContext context) {
        if (phase == PeaPodPlantFood.Phase.NONE) {
            return;
        }
        ticksRemaining--;
        if (!fired) {
            windupRemaining--;
            if (windupRemaining <= 0) {
                fireBubble(plant, context);
                fired = true;
            }
        }
        if (ticksRemaining > 0) {
            return;
        }
        phase = PeaPodPlantFood.Phase.NONE;
        ticksRemaining = 0;
    }

    private void fireBubble(Plant plant, GameContext context) {
        if (context == null) {
            return;
        }
        context.spawnProjectile(plant, plant.getStats().damage(), 1, ProjectileProfile.piercingProfile());
    }

    public PeaPodPlantFood.Phase phase() {
        return phase;
    }

    public boolean isActive() {
        return phase != PeaPodPlantFood.Phase.NONE;
    }
}
