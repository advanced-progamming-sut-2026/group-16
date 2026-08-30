package io.github.finalwave.model.game.entity.plant.food;

import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.plant.Plant;

public final class SunBeanPlantFoodEffect implements PlantFoodEffect {

    private static final int DURATION_TICKS = 22;
    private static final int SETUP_TICKS = 5;
    private int tickTimer;
    private boolean applied;

    public SunBeanPlantFoodEffect(double armorValue) {
    }

    @Override
    public void apply(Plant plant, GameContext context) {
        tickTimer = 0;
        applied = false;
        plant.beginPlantFood(DURATION_TICKS, SETUP_TICKS);
    }

    @Override
    public void tick(Plant plant, GameContext context) {
        tickTimer++;
        if (!applied && tickTimer >= SETUP_TICKS) {
            plant.setSunBeanPowered(true);
            applied = true;
        }
    }

    @Override
    public void end(Plant plant, GameContext context) {
        tickTimer = 0;
        applied = false;
    }
}
