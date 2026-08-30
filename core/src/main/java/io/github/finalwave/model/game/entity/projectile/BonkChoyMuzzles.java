package io.github.finalwave.model.game.entity.projectile;

import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.entity.plant.ability.BonkChoyAbility;


public final class BonkChoyMuzzles {

    public static final float PUNCH_INTERVAL_SECONDS = 0.33f;
    public static final float ATTACK_SECONDS = 0.3333f;
    public static final float ATTACK3_SECONDS = 0.6667f;
    public static final float ATTACK4_SECONDS = 0.5f;
    public static final float ATTACK5_SECONDS = 0.5f;
    public static final float PLANT_FOOD_ON_SECONDS = 1.0f;
    public static final float PLANT_FOOD_LOOP_SECONDS = 1.0f;
    public static final float PLANT_FOOD_OFF_SECONDS = 0.3333f;
    public static final float PLANT_FOOD_DURATION_SECONDS = 3.0f;
    public static final int PLANT_FOOD_TOTAL_DAMAGE = 1500;
    public static final int MELEE_RADIUS = 1;

    private BonkChoyMuzzles() {
    }

    public static int punchIntervalTicks() {
        return secondsToTicks(PUNCH_INTERVAL_SECONDS);
    }

    public static int plantFoodOnTicks() {
        return secondsToTicks(PLANT_FOOD_ON_SECONDS);
    }

    public static int plantFoodLoopTicks() {
        return secondsToTicks(PLANT_FOOD_LOOP_SECONDS);
    }

    public static int plantFoodOffTicks() {
        return secondsToTicks(PLANT_FOOD_OFF_SECONDS);
    }

    public static int plantFoodDurationTicks() {
        return secondsToTicks(PLANT_FOOD_DURATION_SECONDS);
    }

    public static int windupTicks(BonkChoyAbility.PunchStyle style) {
        return switch (style) {
            case BOTH -> secondsToTicks(ATTACK3_SECONDS);
            case UP_RIGHT -> secondsToTicks(ATTACK4_SECONDS);
            case UP_LEFT -> secondsToTicks(ATTACK5_SECONDS);
            default -> secondsToTicks(ATTACK_SECONDS);
        };
    }

    public static int plantFoodPulseCount() {
        int duration = plantFoodDurationTicks();
        int interval = punchIntervalTicks();
        return Math.max(1, duration / interval);
    }

    public static int plantFoodDamagePerPulse() {
        return Math.max(1, PLANT_FOOD_TOTAL_DAMAGE / plantFoodPulseCount());
    }

    private static int secondsToTicks(float seconds) {
        return Math.max(1, Math.round(seconds * GameSession.TICKS_PER_SECOND));
    }
}
