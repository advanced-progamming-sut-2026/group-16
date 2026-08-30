package io.github.finalwave.model.game.entity.projectile;

import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.entity.plant.Plant;


public final class EndurianMuzzles {

    public static final float ATTACK_START_SECONDS = 0.5f;
    public static final float ATTACK_LOOP_SECONDS = 0.3333f;
    public static final float ATTACK_END_SECONDS = 0.5333f;
    public static final float PLANT_FOOD_ON_SECONDS = 0.5667f;
    public static final int RADIUS = 1;
    public static final int ARMORED_DAMAGE_MULTIPLIER = 2;

    private EndurianMuzzles() {
    }

    public static int spikeDamage(Plant plant) {
        int damage = plant.getStats().damage();
        if (plant.hasSmashArmor()) {
            return damage * ARMORED_DAMAGE_MULTIPLIER;
        }
        return damage;
    }

    public static int pulseTicks() {
        return secondsToTicks(ATTACK_START_SECONDS);
    }

    public static int attackDurationTicks() {
        return secondsToTicks(ATTACK_START_SECONDS)
                + secondsToTicks(ATTACK_LOOP_SECONDS)
                + secondsToTicks(ATTACK_END_SECONDS);
    }

    public static int plantFoodOnTicks() {
        return secondsToTicks(PLANT_FOOD_ON_SECONDS);
    }

    private static int secondsToTicks(float seconds) {
        return Math.max(1, Math.round(seconds * GameSession.TICKS_PER_SECOND));
    }
}
