package io.github.finalwave.model.game.entity.plant.ability;

import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.plant.Plant;

public interface PlantAbility {

    default void onPlanted(Plant plant, GameContext context) {
    }

    default void onActionReady(Plant plant, GameContext context) {
    }
}
