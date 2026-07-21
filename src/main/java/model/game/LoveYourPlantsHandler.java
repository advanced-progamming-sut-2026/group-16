package model.game;

import model.adventure.LevelType;

public final class LoveYourPlantsHandler implements SpecialLevelHandler {
    @Override
    public LevelType getLevelType() {
        return LevelType.LOVE_YOUR_PLANTS;
    }
}
