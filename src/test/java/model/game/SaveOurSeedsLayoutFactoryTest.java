package model.game;

import model.adventure.AdventureRegistry;
import model.adventure.ChapterConfig;
import model.adventure.ChapterId;
import model.adventure.LevelConfig;
import model.adventure.LevelType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SaveOurSeedsLayoutFactoryTest {

    @Test
    void frostbiteLevel2ReturnsDemoWallNutLayout() {
        ChapterConfig chapter = AdventureRegistry.getInstance().getChapter(ChapterId.FROSTBITE_CAVES);
        LevelConfig level = chapter.getLevel(2);

        SaveOurSeedsLayout layout = SaveOurSeedsLayoutFactory.create(chapter, level);

        assertEquals(2, layout.getPlacements().size());
        SeedPlacement first = layout.getPlacements().get(0);
        SeedPlacement second = layout.getPlacements().get(1);
        assertEquals("Wall-nut", first.getPlantName());
        assertEquals(2, first.getCol());
        assertEquals(1, first.getRow());
        assertEquals("Wall-nut", second.getPlantName());
        assertEquals(2, second.getCol());
        assertEquals(3, second.getRow());
        assertEquals(List.of(1, 3), layout.getDangerRows());
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
