package model.game;

import model.adventure.LevelType;

public final class TimedWarHandler implements SpecialLevelHandler {
    @Override
    public LevelType getLevelType() {
        return LevelType.TIMED_WAR;
    }
}
