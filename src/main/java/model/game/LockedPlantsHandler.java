package model.game;

import model.adventure.LevelType;

public final class LockedPlantsHandler implements SpecialLevelHandler {
    @Override
    public LevelType getLevelType() {
        return LevelType.LOCKED_PLANTS;
    }
}
