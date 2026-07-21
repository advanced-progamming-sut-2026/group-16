package model.game;

import model.adventure.ChapterConfig;
import model.adventure.LevelConfig;
import model.adventure.LevelType;

public final class TimedWarRulesFactory {

    private static final int DEMO_DURATION_SECONDS = 60;
    private static final int KILL_GOAL = 3;
    private static final int SUN_GOAL = 150;

    private TimedWarRulesFactory() {
    }

    public static TimedWarRules create(ChapterConfig chapter, LevelConfig level) {
        if (chapter == null || level == null) {
            throw new IllegalArgumentException("chapter and level must not be null");
        }
        String handlerKey = level.getSpecialHandlerKey();
        if (handlerKey == null) {
            if (level.getType() == LevelType.TIMED_WAR) {
                throw new IllegalArgumentException("Unknown Timed War handler key: null");
            }
            return inertRules();
        }
        return switch (handlerKey) {
            case "timed" -> new TimedWarRules(
                    TimedWarMode.KILL,
                    DEMO_DURATION_SECONDS * GameSession.TICKS_PER_SECOND,
                    KILL_GOAL);
            case "timed-sun" -> new TimedWarRules(
                    TimedWarMode.SUN,
                    DEMO_DURATION_SECONDS * GameSession.TICKS_PER_SECOND,
                    SUN_GOAL);
            default -> {
                if (level.getType() == LevelType.TIMED_WAR) {
                    throw new IllegalArgumentException("Unknown Timed War handler key: " + handlerKey);
                }
                yield inertRules();
            }
        };
    }

    private static TimedWarRules inertRules() {
        return new TimedWarRules(TimedWarMode.KILL, 0, 0);
    }
}
