package io.github.finalwave.model.game.entity.plant.food;

import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.plant.Plant;

public final class SeaShroomPlantFoodEffect implements PlantFoodEffect {

    private static final int DURATION_TICKS = 35;

    @Override
    public void apply(Plant plant, GameContext context) {
        plant.beginPlantFood(DURATION_TICKS, 0, 0);
        context.spawnClones(plant, 1);
        ShroomFamilyPlantFoodSupport.resetFamilyLifespan(context, "Sea-shroom");
        ShroomFamilyPlantFoodSupport.triggerFamilyShots(context, "Sea-shroom");
    }

    @Override
    public void tick(Plant plant, GameContext context) {
    }

    @Override
    public void end(Plant plant, GameContext context) {
    }
}
