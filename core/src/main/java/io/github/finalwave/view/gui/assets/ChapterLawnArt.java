package io.github.finalwave.view.gui.assets;

import io.github.finalwave.model.adventure.ChapterId;


public record ChapterLawnArt(float lawnX, float lawnY, float tileWidth, float tileHeight) {
    private static final ChapterLawnArt ANCIENT_EGYPT = new ChapterLawnArt(252.3f, 77.1f, 81.85f, 98.83f);
    private static final ChapterLawnArt FROSTBITE_CAVES = new ChapterLawnArt(252.9f, 95.7f, 82.07f, 96.31f);
    private static final ChapterLawnArt BIG_WAVE_BEACH = new ChapterLawnArt(256.0f, 87.2f, 81.73f, 96.26f);
    private static final ChapterLawnArt DARK_AGES = new ChapterLawnArt(251.6f, 80.3f, 82.70f, 97.09f);

    public static ChapterLawnArt of(ChapterId chapterId) {
        if (chapterId == null) {
            return ANCIENT_EGYPT;
        }
        return switch (chapterId) {
            case ANCIENT_EGYPT -> ANCIENT_EGYPT;
            case FROSTBITE_CAVES -> FROSTBITE_CAVES;
            case BIG_WAVE_BEACH -> BIG_WAVE_BEACH;
            case DARK_AGES -> DARK_AGES;
        };
    }

    public float centerX(int cols) {
        return lawnX + Math.max(1, cols) * tileWidth / 2f;
    }
}
