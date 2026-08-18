package io.github.finalwave.model.game;

import io.github.finalwave.model.adventure.AdventureRegistry;
import io.github.finalwave.model.adventure.ChapterConfig;
import io.github.finalwave.model.adventure.ChapterId;
import io.github.finalwave.model.adventure.LevelConfig;
import io.github.finalwave.model.adventure.LevelType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TimedWarRulesFactoryTest {

    @Test
    void timedKeyCreatesKillModeDemoRules() {
        ChapterConfig chapter = AdventureRegistry.getInstance().getChapter(ChapterId.FROSTBITE_CAVES);
        LevelConfig level = chapter.getLevel(3);

        TimedWarRules rules = TimedWarRulesFactory.create(chapter, level);

        assertEquals(TimedWarMode.KILL, rules.getMode());
        assertEquals(60, rules.getDurationSeconds());
        assertEquals(60 * GameSession.TICKS_PER_SECOND, rules.getDurationTicks());
        assertEquals(3, rules.getGoalAmount());
    }

    @Test
    void timedSunKeyCreatesSunModeDemoRules() {
        ChapterConfig chapter = AdventureRegistry.getInstance().getChapter(ChapterId.FROSTBITE_CAVES);
        LevelConfig level = chapter.getLevel(4);

        TimedWarRules rules = TimedWarRulesFactory.create(chapter, level);

        assertEquals(TimedWarMode.SUN, rules.getMode());
        assertEquals(60, rules.getDurationSeconds());
        assertEquals(60 * GameSession.TICKS_PER_SECOND, rules.getDurationTicks());
        assertEquals(150, rules.getGoalAmount());
    }

    @Test
    void unknownTimedWarHandlerKeyThrows() {
        ChapterConfig chapter = AdventureRegistry.getInstance().getChapter(ChapterId.FROSTBITE_CAVES);
        LevelConfig bad = new LevelConfig(
                3, LevelType.TIMED_WAR, 3, 8, 50, 300, List.of("ZombieDefault"), "timed-unknown");

        assertThrows(IllegalArgumentException.class,
                () -> TimedWarRulesFactory.create(chapter, bad));
    }
}
