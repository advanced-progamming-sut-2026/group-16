package io.github.finalwave.model.game;

import io.github.finalwave.model.adventure.ChapterConfig;
import io.github.finalwave.model.adventure.ChapterId;
import io.github.finalwave.model.adventure.LevelConfig;
import io.github.finalwave.model.adventure.LevelType;
import io.github.finalwave.model.game.board.GameBoard;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class SaveOurSeedsLayoutFactory {

    private static final int PROTECTED_COLUMN = 2;

    private static final Map<String, List<SeedPlacement>> LAYOUTS = Map.of(
            chapterLevelKey(ChapterId.FROSTBITE_CAVES, 2), wallNutColumn(PROTECTED_COLUMN));

    private SaveOurSeedsLayoutFactory() {
    }

    public static SaveOurSeedsLayout create(ChapterConfig chapter, LevelConfig level) {
        if (chapter == null || level == null) {
            throw new IllegalArgumentException("chapter and level must not be null");
        }
        String handlerKey = level.getSpecialHandlerKey();
        if (handlerKey == null || !"sos".equals(handlerKey)) {
            if (level.getType() == LevelType.SAVE_OUR_SEEDS) {
                throw new IllegalArgumentException(
                        "Unknown Save Our Seeds handler key: " + handlerKey);
            }
            return new SaveOurSeedsLayout(List.of());
        }
        List<SeedPlacement> placements = LAYOUTS.get(
                chapterLevelKey(chapter.getId(), level.getIndex()));
        if (placements == null) {
            throw new IllegalArgumentException(
                    "No Save Our Seeds layout for " + chapter.getId() + " level " + level.getIndex());
        }
        return new SaveOurSeedsLayout(placements);
    }

    private static List<SeedPlacement> wallNutColumn(int col) {
        List<SeedPlacement> placements = new ArrayList<>();
        for (int row = 0; row < GameBoard.DEFAULT_ROWS; row++) {
            placements.add(new SeedPlacement("Wall-nut", col, row));
        }
        return List.copyOf(placements);
    }

    private static String chapterLevelKey(ChapterId chapterId, int levelIndex) {
        return chapterId.name() + ":" + levelIndex;
    }
}
