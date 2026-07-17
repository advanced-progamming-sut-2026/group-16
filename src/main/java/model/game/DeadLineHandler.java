package model.game;

import model.adventure.LevelType;

public final class DeadLineHandler implements SpecialLevelHandler {
    @Override
    public LevelType getLevelType() {
        return LevelType.DEAD_LINE;
    }
}
