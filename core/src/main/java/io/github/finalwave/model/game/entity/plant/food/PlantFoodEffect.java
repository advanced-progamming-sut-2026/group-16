package io.github.finalwave.model.game.entity.plant.food;

import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.plant.Plant;

public interface PlantFoodEffect {

    void apply(Plant plant, GameContext context);

    default void tick(Plant plant, GameContext context) {
    }

    default void end(Plant plant, GameContext context) {
    }
}
