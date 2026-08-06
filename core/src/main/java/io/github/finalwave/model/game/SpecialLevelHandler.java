package io.github.finalwave.model.game;

import io.github.finalwave.model.adventure.LevelType;
import io.github.finalwave.model.game.entity.plant.Plant;

public interface SpecialLevelHandler {

    LevelType getLevelType();

    default void onLevelStart(GameSession session) {
    }

    default void onWaveStarted(GameSession session, int waveNumber) {
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
