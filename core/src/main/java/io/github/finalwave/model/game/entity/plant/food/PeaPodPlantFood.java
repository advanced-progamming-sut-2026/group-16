package io.github.finalwave.model.game.entity.plant.food;

import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.plant.Plant;


public final class PeaPodPlantFood {
    public enum Phase {
        NONE,
        ON,
        LOOP,
        OFF
    }

    public static final int ON_TICKS = 15;
    public static final int GIANT_SHOTS = 5;
    public static final int GIANT_WINDUP_TICKS = 5;
    public static final int GIANT_STAGGER_TICKS = 11;
    public static final int LOOP_TICKS = GIANT_WINDUP_TICKS + (GIANT_SHOTS - 1) * GIANT_STAGGER_TICKS;
    public static final int OFF_TICKS = 8;

    private Phase phase = Phase.NONE;
    private int ticksRemaining;
    private int damage;
    private int giantsRemaining;
    private int giantStaggerRemaining;

    public boolean start(int damage) {
        if (phase != Phase.NONE) {
            return false;
        }
        this.damage = Math.max(1, damage);
        phase = Phase.ON;
        ticksRemaining = ON_TICKS;
        giantsRemaining = GIANT_SHOTS;
        giantStaggerRemaining = 0;
        return true;
    }

    public void tick(Plant plant, GameContext context) {
        if (phase == Phase.NONE) {
            return;
        }
        ticksRemaining--;
        if (phase == Phase.LOOP) {
            tickGiantVolley(plant, context);
        }
        if (ticksRemaining > 0) {
            return;
        }
        if (phase == Phase.ON) {
            phase = Phase.LOOP;
            ticksRemaining = LOOP_TICKS;
            giantStaggerRemaining = GIANT_WINDUP_TICKS;
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

    private void tickGiantVolley(Plant plant, GameContext context) {
        if (giantsRemaining <= 0) {
            return;
        }
        giantStaggerRemaining--;
        if (giantStaggerRemaining > 0) {
            return;
        }
        fireGiant(plant, context);
        giantStaggerRemaining = GIANT_STAGGER_TICKS;
    }

    private void fireGiant(Plant plant, GameContext context) {
        if (giantsRemaining <= 0) {
            return;
        }
        context.spawnPeaPodGiant(plant, damage);
        giantsRemaining--;
    }

    public Phase phase() {
        return phase;
    }

    public boolean isActive() {
        return phase != Phase.NONE;
    }
}
