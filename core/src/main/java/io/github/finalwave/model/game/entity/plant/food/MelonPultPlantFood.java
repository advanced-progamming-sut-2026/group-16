package io.github.finalwave.model.game.entity.plant.food;

import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.projectile.MelonMuzzles;


public final class MelonPultPlantFood {
    public static final int LOOP_TICKS = 29;
    public static final int[] SHOT_TICKS = MelonMuzzles.PLANTFOOD_SHOT_TICKS;

    private PeaPodPlantFood.Phase phase = PeaPodPlantFood.Phase.NONE;
    private int ticksRemaining;
    private int elapsedTicks;
    private int nextShot;
    private int damage;

    public boolean start(int damage) {
        if (phase != PeaPodPlantFood.Phase.NONE) {
            return false;
        }
        this.damage = Math.max(1, damage);
        phase = PeaPodPlantFood.Phase.LOOP;
        ticksRemaining = LOOP_TICKS;
        elapsedTicks = 0;
        nextShot = 0;
        return true;
    }

    public void tick(Plant plant, GameContext context) {
        if (phase == PeaPodPlantFood.Phase.NONE) {
            return;
        }
        ticksRemaining--;
        elapsedTicks++;
        if (nextShot < SHOT_TICKS.length && elapsedTicks == SHOT_TICKS[nextShot]) {
            fireMelon(plant, context);
            nextShot++;
        }
        if (ticksRemaining > 0) {
            return;
        }
        phase = PeaPodPlantFood.Phase.NONE;
        ticksRemaining = 0;
    }

    private void fireMelon(Plant plant, GameContext context) {
        if (context == null) {
            return;
        }
        if (plant != null && plant.isWinterMelon()) {
            context.spawnWinterMelonPlantFood(plant, damage);
            return;
        }
        context.spawnMelonPlantFood(plant, damage);
    }

    public PeaPodPlantFood.Phase phase() {
        return phase;
    }

    public boolean isActive() {
        return phase != PeaPodPlantFood.Phase.NONE;
    }
}
