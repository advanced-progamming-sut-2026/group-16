package io.github.finalwave.model.game.entity.plant.food;

import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.plant.Plant;


public final class PotatoMinePlantFood {
    public static final int ON_TICKS = 6;
    public static final int LOOP_TICKS = 5;
    public static final int OFF_TICKS = 4;

    private Phase phase = Phase.NONE;
    private int ticksRemaining;
    private int cloneCount;

    public boolean start(int cloneCount) {
        if (phase != Phase.NONE) {
            return false;
        }
        this.cloneCount = Math.max(1, cloneCount);
        phase = Phase.ON;
        ticksRemaining = ON_TICKS;
        return true;
    }

    public void tick(Plant plant, GameContext context) {
        if (phase == Phase.NONE) {
            return;
        }
        ticksRemaining--;
        if (ticksRemaining > 0) {
            return;
        }
        if (phase == Phase.ON) {
            phase = Phase.LOOP;
            ticksRemaining = LOOP_TICKS;
            context.spawnForwardClones(plant, cloneCount);
            return;
        }
        if (phase == Phase.LOOP) {
            phase = Phase.OFF;
            ticksRemaining = OFF_TICKS;
            return;
        }
        phase = Phase.NONE;
        ticksRemaining = 0;
    }

    public Phase phase() {
        return phase;
    }

    public boolean isActive() {
        return phase != Phase.NONE;
    }

    public enum Phase {
        NONE,
        ON,
        LOOP,
        OFF
    }
}
