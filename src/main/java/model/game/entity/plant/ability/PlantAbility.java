package model.game.entity.plant.ability;

import model.game.entity.GameContext;
import model.game.entity.plant.Plant;

public interface PlantAbility {

    default void onPlanted(Plant plant, GameContext context) {
    }

    default void onActionReady(Plant plant, GameContext context) {
    }
}
