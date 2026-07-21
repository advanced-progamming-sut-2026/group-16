package model.game;

import model.adventure.LevelType;

public final class ConveyBeltHandler implements SpecialLevelHandler {
    @Override
    public LevelType getLevelType() {
        return LevelType.CONVEYOR_BELT;
    }
}
