package io.github.finalwave.model.game.entity.projectile;

import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.entity.plant.ability.TangleKelpAbility;


public final class TangleKelpMuzzles {

    public static final float SUBMERGE_SECONDS = 2.1333f;
    public static final float ATTACK_SECONDS = 2.4667f;
    public static final float EMERGE_SECONDS = 1.8333f;
    public static final float PLANT_FOOD_ON_SECONDS = 2.0f;
    public static final float PLANT_FOOD_SECONDS = 2.1f;
    public static final float PLANT_FOOD_OFF_SECONDS = 0.6667f;

    private TangleKelpMuzzles() {
    }

    public static int phaseTicks(TangleKelpAbility.Phase phase) {
        return switch (phase) {
            case SUBMERGE -> secondsToTicks(SUBMERGE_SECONDS);
            case ATTACK -> secondsToTicks(ATTACK_SECONDS);
            case EMERGE -> secondsToTicks(EMERGE_SECONDS);
            case PLANT_FOOD_ON -> secondsToTicks(PLANT_FOOD_ON_SECONDS);
            case PLANT_FOOD -> secondsToTicks(PLANT_FOOD_SECONDS);
            case PLANT_FOOD_OFF -> secondsToTicks(PLANT_FOOD_OFF_SECONDS);
            default -> 0;
        };
    }

    public static int fullGrabTicks() {
        return phaseTicks(TangleKelpAbility.Phase.SUBMERGE)
                + phaseTicks(TangleKelpAbility.Phase.ATTACK)
                + phaseTicks(TangleKelpAbility.Phase.EMERGE)
                + 3;
    }

    public static int fullPlantFoodTicks() {
        return phaseTicks(TangleKelpAbility.Phase.PLANT_FOOD_ON)
                + phaseTicks(TangleKelpAbility.Phase.PLANT_FOOD)
                + phaseTicks(TangleKelpAbility.Phase.PLANT_FOOD_OFF)
                + 3;
    }

    private static int secondsToTicks(float seconds) {
        return Math.max(1, Math.round(seconds * GameSession.TICKS_PER_SECOND));
    }
}
