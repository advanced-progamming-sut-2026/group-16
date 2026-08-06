package io.github.finalwave.model.scoregame;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class ScoreGameDailySeedTest {

    @Test
    void sameUtcDateYieldsSameSeed() {
        Clock clock = Clock.fixed(Instant.parse("2026-07-23T12:00:00Z"), ZoneOffset.UTC);
        assertEquals(ScoreGameDailySeed.forClock(clock), ScoreGameDailySeed.forClock(clock));
        assertEquals(LocalDate.of(2026, 7, 23).toEpochDay(), ScoreGameDailySeed.forClock(clock));
    }

    @Test
    void differentDatesYieldDifferentSeeds() {
        long day1 = ScoreGameDailySeed.forDate(LocalDate.of(2026, 7, 23));
        long day2 = ScoreGameDailySeed.forDate(LocalDate.of(2026, 7, 24));
        assertNotEquals(day1, day2);
    }
}
