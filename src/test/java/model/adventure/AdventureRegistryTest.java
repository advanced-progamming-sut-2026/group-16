package model.adventure;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AdventureRegistryTest {

    @Test
    void hasFourChaptersWithFourLevelsEach() {
        AdventureRegistry registry = AdventureRegistry.getInstance();
        assertEquals(4, registry.getAllChapters().size());
        for (ChapterConfig chapter : registry.getAllChapters()) {
            assertEquals(4, chapter.getLevels().size());
            assertNotNull(chapter.getFirstNormalLevel());
            assertEquals(LevelType.NORMAL, chapter.getLevel(1).getType());
            assertEquals(LevelType.BOSS, chapter.getLevel(4).getType());
            assertTrue(chapter.getLevel(2).getType().isSpecial());
            assertTrue(chapter.getLevel(3).getType().isSpecial());
        }
    }

    @Test
    void resolvesChapterByName() {
        assertNotNull(AdventureRegistry.getInstance().getChapterByName("Ancient Egypt"));
        assertNotNull(AdventureRegistry.getInstance().getChapterByName("dark-ages"));
        assertNull(AdventureRegistry.getInstance().getChapterByName("unknown"));
    }

    @Test
    void specialLevelTypesCoverEightKinds() {
        var types = AdventureRegistry.getInstance().getAllChapters().stream()
                .flatMap(c -> c.getLevels().stream())
                .map(LevelConfig::getType)
                .filter(LevelType::isSpecial)
                .distinct()
                .toList();
        assertEquals(8, types.size());
    }
}
