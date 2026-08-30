package io.github.finalwave.model.game.entity.projectile;

import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.entity.plant.Plant;


public final class KiwibeastMuzzles {

    public static final float STAGE1_PULSE_SECONDS = 0.83f;
    public static final float STAGE2_PULSE_SECONDS = 0.83f;
    public static final float STAGE3_PULSE_SECONDS = 0.77f;
    public static final float STAGE1_ATTACK_SECONDS = 1.4333f;
    public static final float STAGE2_ATTACK_SECONDS = 1.4667f;
    public static final float STAGE3_ATTACK_SECONDS = 1.5333f;
    public static final float[] PLANT_FOOD_PULSE_SECONDS = {0.50f, 1.20f, 2.20f};
    public static final float PLANT_FOOD_SECONDS = 2.9f;
    public static final int PLANT_FOOD_DAMAGE = 350;
    public static final int PLANT_FOOD_RADIUS = 1;
    public static final int STAGE1_DAMAGE = 15;
    public static final int STAGE2_DAMAGE = 25;
    public static final int STAGE3_DAMAGE = 40;
    public static final int STAGE1_RADIUS = 1;
    public static final int STAGE2_RADIUS = 2;
    public static final int STAGE3_RADIUS = 3;
    public static final int STAGE2_DAMAGE_TAKEN = 300;
    public static final int STAGE3_DAMAGE_TAKEN = 1000;

    private KiwibeastMuzzles() {
    }

    public static int hpStage(Plant plant) {
        int taken = Math.max(0, plant.getMaxHealth() - plant.getHealth());
        if (taken >= STAGE3_DAMAGE_TAKEN) {
            return 3;
        }
        if (taken >= STAGE2_DAMAGE_TAKEN) {
            return 2;
        }
        return 1;
    }

    public static int radius(int stage) {
        return switch (clampStage(stage)) {
            case 2 -> STAGE2_RADIUS;
            case 3 -> STAGE3_RADIUS;
            default -> STAGE1_RADIUS;
        };
    }

    public static int stageDamage(Plant plant, int stage) {
        int extra = Math.max(0, plant.getStats().damage() - STAGE1_DAMAGE);
        int base = switch (clampStage(stage)) {
            case 2 -> STAGE2_DAMAGE;
            case 3 -> STAGE3_DAMAGE;
            default -> STAGE1_DAMAGE;
        };
        return base + extra;
    }

    public static int pulseTicks(int stage) {
        return switch (clampStage(stage)) {
            case 2 -> secondsToTicks(STAGE2_PULSE_SECONDS);
            case 3 -> secondsToTicks(STAGE3_PULSE_SECONDS);
            default -> secondsToTicks(STAGE1_PULSE_SECONDS);
        };
    }

    public static int attackDurationTicks(int stage) {
        return switch (clampStage(stage)) {
            case 2 -> secondsToTicks(STAGE2_ATTACK_SECONDS);
            case 3 -> secondsToTicks(STAGE3_ATTACK_SECONDS);
            default -> secondsToTicks(STAGE1_ATTACK_SECONDS);
        };
    }

    public static int plantFoodPulseTicks(int index) {
        int i = Math.max(0, Math.min(PLANT_FOOD_PULSE_SECONDS.length - 1, index));
        return secondsToTicks(PLANT_FOOD_PULSE_SECONDS[i]);
    }

    public static int plantFoodPulseCount() {
        return PLANT_FOOD_PULSE_SECONDS.length;
    }

    public static int plantFoodDurationTicks() {
        return secondsToTicks(PLANT_FOOD_SECONDS);
    }

    public static int clampStage(int stage) {
        return Math.max(1, Math.min(3, stage));
    }

    private static int secondsToTicks(float seconds) {
        return Math.max(1, Math.round(seconds * GameSession.TICKS_PER_SECOND));
    }
}
