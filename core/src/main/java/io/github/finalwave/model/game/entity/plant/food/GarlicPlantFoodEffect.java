package io.github.finalwave.model.game.entity.plant.food;

import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.plant.support.PlantLaneSupport;

import java.util.concurrent.ThreadLocalRandom;

public final class GarlicPlantFoodEffect implements PlantFoodEffect {

    private static final int DURATION_TICKS = 20;
    private static final int SETUP_TICKS = 4;
    private int tickTimer;
    private boolean applied;

    @Override
    public void apply(Plant plant, GameContext context) {
        tickTimer = 0;
        applied = false;
        plant.restoreHealth(plant.getMaxHealth());
        plant.beginPlantFood(DURATION_TICKS, SETUP_TICKS);
    }

    @Override
    public void tick(Plant plant, GameContext context) {
        tickTimer++;
        if (!applied && tickTimer >= SETUP_TICKS) {
            PlantLaneSupport.divertRowZombies(plant, context, ThreadLocalRandom.current());
            applied = true;
        }
    }

    @Override
    public void end(Plant plant, GameContext context) {
        tickTimer = 0;
        applied = false;
    }
}
