package model.game;

import model.adventure.LevelType;

public interface SpecialLevelHandler {

    LevelType getLevelType();

    default void onLevelStart(GameSession session) {
    }

    default void onWaveStarted(GameSession session, int waveNumber) {
    }

    default void onLevelWon(GameSession session) {
    }

    default void onLevelLost(GameSession session) {
    }
}
