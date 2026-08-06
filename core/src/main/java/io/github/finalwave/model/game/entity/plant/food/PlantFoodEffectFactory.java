package io.github.finalwave.model.game.entity.plant.food;

import io.github.finalwave.model.definition.plant.PlantDefinition;

public final class PlantFoodEffectFactory {

    private PlantFoodEffectFactory() {
    }

    public static PlantFoodEffect create(PlantDefinition definition) {
        PlantFoodType type = PlantFoodType.fromDefinition(definition.getPlantFoodType());
        return new GenericPlantFoodEffect(type, definition.getPlantFoodValue());
    }
}
