package io.github.finalwave.model.adventure;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AdventureRegistryTest {

    @Test
    void hasFourChaptersWithPlayableTracksAndABoss() {
        AdventureRegistry registry = AdventureRegistry.getInstance();
        assertEquals(4, registry.getAllChapters().size());
        for (ChapterConfig chapter : registry.getAllChapters()) {
            List<LevelConfig> levels = chapter.getLevels();
            assertTrue(levels.size() >= 4);
            assertNotNull(chapter.getFirstNormalLevel());
            assertEquals(LevelType.NORMAL, chapter.getLevel(1).getType());
            LevelConfig last = levels.get(levels.size() - 1);
            assertEquals(LevelType.BOSS, last.getType());
            assertTrue(chapter.getLevel(2).getType().isSpecial());
            assertTrue(chapter.getLevel(3).getType().isSpecial());
        }
        ChapterConfig frostbite = registry.getChapter(ChapterId.FROSTBITE_CAVES);
        assertEquals(5, frostbite.getLevels().size());
        assertEquals(LevelType.TIMED_WAR, frostbite.getLevel(3).getType());
        assertEquals("timed", frostbite.getLevel(3).getSpecialHandlerKey());
        assertEquals(LevelType.TIMED_WAR, frostbite.getLevel(4).getType());
        assertEquals("timed-sun", frostbite.getLevel(4).getSpecialHandlerKey());
        assertEquals(LevelType.BOSS, frostbite.getLevel(5).getType());
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
