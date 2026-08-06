package io.github.finalwave.model.minigame;

import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.entity.plant.Plant;

public interface MiniGameHandler {

    default void onLevelStart(GameSession session) {
    }

    default void onTick(GameSession session) {
    }

    default void onPlantLost(GameSession session, Plant plant) {
    }

    default void onLevelWon(GameSession session) {
    }

    default void onLevelLost(GameSession session) {
    }
}
