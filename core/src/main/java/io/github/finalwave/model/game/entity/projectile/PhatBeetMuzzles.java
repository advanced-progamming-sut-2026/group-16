package io.github.finalwave.model.game.entity.projectile;

import io.github.finalwave.model.game.GameSession;


public final class PhatBeetMuzzles {

    public static final float ATTACK_PULSE_SECONDS = 0.5f;
    public static final float PLANT_FOOD_PULSE_SECONDS = 1.20f;
    public static final float PLANT_FOOD_SECONDS = 2.0f;
    public static final int INNER_RADIUS = 1;
    public static final int OUTER_RADIUS = 2;
    public static final int INNER_PLANT_FOOD_DAMAGE = 400;
    public static final int OUTER_PLANT_FOOD_DAMAGE = 200;
    public static final int CRIT_MULTIPLIER = 3;
    public static final int MIN_ATTACKS_TO_CRIT = 4;
    public static final int MAX_ATTACKS_TO_CRIT = 6;

    private PhatBeetMuzzles() {
    }

    public static int attackPulseTicks() {
        return secondsToTicks(ATTACK_PULSE_SECONDS);
    }

    public static int plantFoodPulseTicks() {
        return secondsToTicks(PLANT_FOOD_PULSE_SECONDS);
    }

    public static int plantFoodDurationTicks() {
        return secondsToTicks(PLANT_FOOD_SECONDS);
    }

    private static int secondsToTicks(float seconds) {
        return Math.max(1, Math.round(seconds * GameSession.TICKS_PER_SECOND));
    }
}
