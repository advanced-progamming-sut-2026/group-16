package io.github.finalwave.model.game.entity.plant.food;

import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.plant.Plant;


public final class WallNutPlantFood {

    public static final float PLANT_FOOD_ON_SECONDS = 0.6667f;
    public static final int DEFAULT_ARMOR = 8000;

    public enum Phase {
        NONE,
        ON
    }

    private Phase phase = Phase.NONE;
    private int ticksElapsed;

    public boolean start(Plant plant) {
        if (plant == null) {
            return false;
        }
        int armor = (int) Math.round(plant.getDefinition().getPlantFoodValue());
        if (armor <= 0) {
            armor = DEFAULT_ARMOR;
        }
        plant.grantSmashArmor(armor);
        phase = Phase.ON;
        ticksElapsed = 0;
        return true;
    }

    public void tick(Plant plant, GameContext context) {
        if (phase == Phase.NONE || plant == null || plant.isDead()) {
            return;
        }
        ticksElapsed++;
        if (ticksElapsed >= plantFoodOnTicks()) {
            phase = Phase.NONE;
            ticksElapsed = 0;
        }
    }

    public Phase phase() {
        return phase;
    }

    public boolean isActive() {
        return phase != Phase.NONE;
    }

    public static int plantFoodOnTicks() {
        return Math.max(1, Math.round(PLANT_FOOD_ON_SECONDS * GameSession.TICKS_PER_SECOND));
    }
}
