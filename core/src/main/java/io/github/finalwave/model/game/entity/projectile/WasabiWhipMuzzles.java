package io.github.finalwave.model.game.entity.projectile;

import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.plant.PlantSpecialModifiers;
import io.github.finalwave.model.game.entity.plant.ability.WasabiWhipAbility;


public final class WasabiWhipMuzzles {

    public static final float BEHIND_SECONDS = 0.6f;
    public static final float AHEAD_SECONDS = 0.7f;
    public static final float PUNCH_INTERVAL_SECONDS = 0.33f;
    public static final float PLANT_FOOD_ON_SECONDS = 1.0f;
    public static final float PLANT_FOOD_LOOP_SECONDS = 1.0f;
    public static final float PLANT_FOOD_OFF_SECONDS = 0.3333f;
    public static final float PLANT_FOOD_DURATION_SECONDS = 3.0f;
    public static final int PLANT_FOOD_TOTAL_DAMAGE = 1500;

    private WasabiWhipMuzzles() {
    }

    public static int rangeTiles(Plant plant) {
        int base = Math.max(1, (int) Math.round(plant.getDefinition().getAbilityValue()));
        int extra = (int) plant.getStats().specialModifier(PlantSpecialModifiers.TILE_RANGE_EXT);
        return Math.max(1, base + extra);
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

    public static int windupTicks(WasabiWhipAbility.WhipStyle style) {
        return switch (style) {
            case LEFT, UP_LEFT, DOWN_LEFT -> secondsToTicks(BEHIND_SECONDS);
            default -> secondsToTicks(AHEAD_SECONDS);
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
