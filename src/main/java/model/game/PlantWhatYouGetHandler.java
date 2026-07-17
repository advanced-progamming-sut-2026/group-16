package model.game;

import model.adventure.LevelType;

public final class PlantWhatYouGetHandler implements SpecialLevelHandler {
    @Override
    public LevelType getLevelType() {
        return LevelType.PLANT_WHAT_YOU_GET;
    }
}
