package model.minigame;

import model.game.GameSession;

public interface MiniGameHandler {

    default void onLevelStart(GameSession session) {
    }

    default void onTick(GameSession session) {
    }

    default void onLevelWon(GameSession session) {
    }

    default void onLevelLost(GameSession session) {
    }
}
