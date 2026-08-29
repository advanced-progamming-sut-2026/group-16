package io.github.finalwave.model.game.entity.plant.ability;

import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.plant.Plant;

public interface PlantAbility {

    default void onPlanted(Plant plant, GameContext context) {
    }

    default void onActionReady(Plant plant, GameContext context) {
    }

    default boolean tryAction(Plant plant, GameContext context) {
        onActionReady(plant, context);
        return true;
    }

    default int actionWindupTicks() {
        return 0;
    }

    default void onConsumeDelayFinished(Plant plant, GameContext context) {
        plant.consumeInstantly();
    }

    default void onTick(Plant plant, GameContext context) {
    }
}
