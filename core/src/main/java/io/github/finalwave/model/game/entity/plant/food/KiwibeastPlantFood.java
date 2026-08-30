package io.github.finalwave.model.game.entity.plant.food;

import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.plant.ability.KiwibeastAbility;
import io.github.finalwave.model.game.entity.projectile.KiwibeastMuzzles;


public final class KiwibeastPlantFood {

    public enum Phase {
        NONE,
        ACTIVE
    }

    private Phase phase = Phase.NONE;
    private int ticksElapsed;
    private int pulsesFired;

    public boolean start(Plant plant, GameContext context) {
        if (phase != Phase.NONE) {
            return false;
        }
        phase = Phase.ACTIVE;
        ticksElapsed = 0;
        pulsesFired = 0;
        if (plant.getAbility() instanceof KiwibeastAbility kiwi) {
            int before = kiwi.stage(plant);
            kiwi.forceMinStage(3);
            if (before < 3) {
                kiwi.notifyGrowth();
                if (context != null) {
                    context.knockbackNearbyZombies(plant, KiwibeastMuzzles.PLANT_FOOD_RADIUS);
                }
            }
        }
        return true;
    }

    public void tick(Plant plant, GameContext context) {
        if (phase == Phase.NONE || plant.isDead()) {
            return;
        }
        ticksElapsed++;
        while (pulsesFired < KiwibeastMuzzles.plantFoodPulseCount()
                && ticksElapsed >= KiwibeastMuzzles.plantFoodPulseTicks(pulsesFired)) {
            context.dealKiwibeastShockwave(
                    plant,
                    KiwibeastMuzzles.PLANT_FOOD_DAMAGE,
                    KiwibeastMuzzles.PLANT_FOOD_RADIUS,
                    true);
            pulsesFired++;
        }
        if (ticksElapsed >= KiwibeastMuzzles.plantFoodDurationTicks()) {
            phase = Phase.NONE;
            ticksElapsed = 0;
            pulsesFired = 0;
            plant.setAttacking(false);
        }
    }

    public Phase phase() {
        return phase;
    }

    public boolean isActive() {
        return phase != Phase.NONE;
    }
}
