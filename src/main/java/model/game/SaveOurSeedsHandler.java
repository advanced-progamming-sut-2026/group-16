package model.game;

import model.adventure.LevelType;

import java.util.List;

public final class SaveOurSeedsHandler implements SpecialLevelHandler {

    private final SaveOurSeedsLayout layout;

    public SaveOurSeedsHandler(SaveOurSeedsLayout layout) {
        this.layout = layout == null ? new SaveOurSeedsLayout(List.of()) : layout;
    }

    public SaveOurSeedsHandler() {
        this(new SaveOurSeedsLayout(List.of()));
    }

    public SaveOurSeedsLayout getLayout() {
        return layout;
    }

    @Override
    public LevelType getLevelType() {
        return LevelType.SAVE_OUR_SEEDS;
    }

    @Override
    public void onLevelStart(GameSession session) {
        for (SeedPlacement placement : layout.getPlacements()) {
            session.placeProtectedSeed(placement.getPlantName(), placement.getCol(), placement.getRow());
        }
    }
}
