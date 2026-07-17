package model.game;

import model.adventure.LevelType;

public interface SpecialLevelHandler {

    LevelType getLevelType();

    default void onLevelStart(GameSession session) {
        throw new UnsupportedOperationException(
                "Special level " + getLevelType() + " is not implemented yet.");
    }
}
