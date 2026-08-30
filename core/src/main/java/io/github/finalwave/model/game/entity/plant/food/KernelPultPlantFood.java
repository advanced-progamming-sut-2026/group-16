package io.github.finalwave.model.game.entity.plant.food;

import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.projectile.KernelMuzzles;


public final class KernelPultPlantFood {
    public static final int LOOP_TICKS = 16;
    public static final int WINDUP_TICKS = KernelMuzzles.PLANTFOOD_WINDUP_TICKS;

    private PeaPodPlantFood.Phase phase = PeaPodPlantFood.Phase.NONE;
    private int ticksRemaining;
    private int windupRemaining;
    private int damage;
    private boolean fired;

    public boolean start(int damage) {
        if (phase != PeaPodPlantFood.Phase.NONE) {
            return false;
        }
        this.damage = Math.max(1, damage);
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
                fireKernel(plant, context);
                fired = true;
            }
        }
        if (ticksRemaining > 0) {
            return;
        }
        phase = PeaPodPlantFood.Phase.NONE;
        ticksRemaining = 0;
    }

    private void fireKernel(Plant plant, GameContext context) {
        if (context == null) {
            return;
        }
        context.spawnKernelPlantFood(plant, damage);
    }

    public PeaPodPlantFood.Phase phase() {
        return phase;
    }

    public boolean isActive() {
        return phase != PeaPodPlantFood.Phase.NONE;
    }
}
