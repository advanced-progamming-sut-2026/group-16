package io.github.finalwave.model.game;

import io.github.finalwave.model.adventure.LevelType;

public record PlantWhatYouGetHandler(int startingSun) implements SpecialLevelHandler {

    public static final int DEFAULT_STARTING_SUN = 500;

    public PlantWhatYouGetHandler {
        if (startingSun < 0) {
            throw new IllegalArgumentException("startingSun must not be negative");
        }
    }

    public PlantWhatYouGetHandler() {
        this(DEFAULT_STARTING_SUN);
    }

    @Override
    public LevelType getLevelType() {
        return LevelType.PLANT_WHAT_YOU_GET;
    }

    @Override
    public void onLevelStart(GameSession session) {
        session.activatePlantWhatYouGet(startingSun);
    }
}
