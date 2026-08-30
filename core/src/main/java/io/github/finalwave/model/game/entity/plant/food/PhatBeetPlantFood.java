package io.github.finalwave.model.game.entity.plant.food;

import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.projectile.PhatBeetMuzzles;


public final class PhatBeetPlantFood {

    public enum Phase {
        NONE,
        ACTIVE
    }

    private Phase phase = Phase.NONE;
    private int ticksElapsed;
    private boolean slammed;

    public boolean start() {
        if (phase != Phase.NONE) {
            return false;
        }
        phase = Phase.ACTIVE;
        ticksElapsed = 0;
        slammed = false;
        return true;
    }

    public void tick(Plant plant, GameContext context) {
        if (phase == Phase.NONE || plant.isDead()) {
            return;
        }
        ticksElapsed++;
        if (!slammed && ticksElapsed >= PhatBeetMuzzles.plantFoodPulseTicks()) {
            context.dealPhatBeetPlantFood(plant);
            slammed = true;
        }
        if (ticksElapsed >= PhatBeetMuzzles.plantFoodDurationTicks()) {
            phase = Phase.NONE;
            ticksElapsed = 0;
            slammed = false;
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
