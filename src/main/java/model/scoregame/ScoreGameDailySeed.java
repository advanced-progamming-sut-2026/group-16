package model.scoregame;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;

public final class ScoreGameDailySeed {
    private ScoreGameDailySeed() {
    }

    public static long forToday() {
        return forClock(Clock.systemUTC());
    }

    public static long forClock(Clock clock) {
        Clock resolved = clock == null ? Clock.systemUTC() : clock;
        return LocalDate.now(resolved.withZone(ZoneOffset.UTC)).toEpochDay();
    }

    public static long forDate(LocalDate date) {
        if (date == null) {
            return forToday();
        }
        return date.toEpochDay();
    }
}
