package io.github.finalwave.model.game;

import io.github.finalwave.model.adventure.AdventureRegistry;
import io.github.finalwave.model.adventure.ChapterConfig;
import io.github.finalwave.model.adventure.ChapterId;
import io.github.finalwave.model.adventure.LevelConfig;
import io.github.finalwave.model.adventure.LevelType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SaveOurSeedsLayoutFactoryTest {

    @Test
    void frostbiteLevel2ReturnsProtectedWallNutColumn() {
        ChapterConfig chapter = AdventureRegistry.getInstance().getChapter(ChapterId.FROSTBITE_CAVES);
        LevelConfig level = chapter.getLevel(2);

        SaveOurSeedsLayout layout = SaveOurSeedsLayoutFactory.create(chapter, level);

        assertEquals(5, layout.getPlacements().size());
        for (int row = 0; row < 5; row++) {
            SeedPlacement placement = layout.getPlacements().get(row);
            assertEquals("Wall-nut", placement.getPlantName());
            assertEquals(2, placement.getCol());
            assertEquals(row, placement.getRow());
        }
        assertEquals(List.of(0, 1, 2, 3, 4), layout.getDangerRows());
    }

    @Test
    void unknownSosHandlerKeyThrows() {
        ChapterConfig chapter = AdventureRegistry.getInstance().getChapter(ChapterId.FROSTBITE_CAVES);
        LevelConfig bad = new LevelConfig(
                2, LevelType.SAVE_OUR_SEEDS, 3, 8, 50, 300, List.of("ZombieDefault"), "sos-unknown");

        assertThrows(IllegalArgumentException.class,
                () -> SaveOurSeedsLayoutFactory.create(chapter, bad));
    }
}
