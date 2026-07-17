package model.game;

import model.adventure.LevelType;

public final class SaveOurSeedsHandler implements SpecialLevelHandler {
    @Override
    public LevelType getLevelType() {
        return LevelType.SAVE_OUR_SEEDS;
    }
}
