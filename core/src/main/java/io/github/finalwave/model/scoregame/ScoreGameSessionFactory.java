package io.github.finalwave.model.scoregame;

import io.github.finalwave.model.adventure.ChapterConfig;
import io.github.finalwave.model.adventure.ChapterId;
import io.github.finalwave.model.adventure.AdventureRegistry;
import io.github.finalwave.model.adventure.LevelConfig;
import io.github.finalwave.model.definition.PlantRegistry;
import io.github.finalwave.model.definition.ZombieRegistry;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.mode.AdventureMode;

import java.time.Clock;
import java.util.Random;

public final class ScoreGameSessionFactory {
    private ScoreGameSessionFactory() {
    }

    public static ScoreGameMatch create(PlantRegistry plantRegistry,
                                        ZombieRegistry zombieRegistry,
                                        Clock clock) {
        long seed = ScoreGameDailySeed.forClock(clock);
        Random random = new Random(seed);
        ChapterConfig chapter = AdventureRegistry.getInstance().getChapter(ChapterId.ANCIENT_EGYPT);
        LevelConfig level = ScoreGameConfig.level();
        AdventureMode mode = new AdventureMode(
                chapter, level, plantRegistry, zombieRegistry,
                ScoreGameConfig.DIFFICULTY, random);
        GameSession session = mode.createSession();
        session.setChapterId(ScoreGameConfig.CHAPTER_ID);
        session.setLevelId(ScoreGameConfig.LEVEL_ID);
        MeowPointTracker tracker = MeowPointTracker.createDefault();
        tracker.registerOn(session.getEventBus());
        return new ScoreGameMatch(mode, session, tracker, chapter, level, seed);
    }

    public record ScoreGameMatch(
            AdventureMode mode,
            GameSession session,
            MeowPointTracker tracker,
            ChapterConfig chapter,
            LevelConfig level,
            long dailySeed
    ) {
    }
}
