package model.scoregame;

import model.adventure.ChapterConfig;
import model.adventure.ChapterId;
import model.adventure.AdventureRegistry;
import model.adventure.LevelConfig;
import model.definition.PlantRegistry;
import model.definition.ZombieRegistry;
import model.game.GameSession;
import model.game.mode.AdventureMode;

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
