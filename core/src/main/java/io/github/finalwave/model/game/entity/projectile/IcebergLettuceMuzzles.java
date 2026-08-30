package io.github.finalwave.model.game.entity.projectile;

import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.entity.plant.ability.IcebergLettuceAbility;


public final class IcebergLettuceMuzzles {

    public static final float ATTACK_SECONDS = 1.5f;
    public static final float PLANT_FOOD_SECONDS = 1.5f;
    public static final float TRAP_FREEZE_SECONDS = 300f;
    public static final float MAP_FREEZE_BASE_SECONDS = 10f;

    private IcebergLettuceMuzzles() {
    }

    public static int attackTicks() {
        return secondsToTicks(ATTACK_SECONDS);
    }

    public static int plantFoodTicks() {
        return secondsToTicks(PLANT_FOOD_SECONDS);
    }

    public static int trapFreezeTicks() {
        return secondsToTicks(TRAP_FREEZE_SECONDS);
    }

    public static int mapFreezeTicks(double baseSeconds, double extensionSeconds) {
        return secondsToTicks((float) (baseSeconds + extensionSeconds));
    }

    public static int phaseTicks(IcebergLettuceAbility.Phase phase) {
        return switch (phase) {
            case ATTACK -> attackTicks();
            case PLANT_FOOD -> plantFoodTicks();
            default -> 0;
        };
    }

    public static int fullTrapTicks() {
        return attackTicks() + 2;
    }

    public static int fullPlantFoodTicks() {
        return plantFoodTicks() + 2;
    }

    private static int secondsToTicks(float seconds) {
        return Math.max(1, Math.round(seconds * GameSession.TICKS_PER_SECOND));
    }
}
