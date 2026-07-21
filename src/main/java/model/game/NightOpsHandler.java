package model.game;

import model.adventure.LevelType;

public final class NightOpsHandler implements SpecialLevelHandler {
    @Override
    public LevelType getLevelType() {
        return LevelType.NIGHT_OPS;
    }
}
