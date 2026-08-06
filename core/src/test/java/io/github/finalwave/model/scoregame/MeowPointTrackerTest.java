package io.github.finalwave.model.scoregame;

import io.github.finalwave.model.quest.event.GameEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MeowPointTrackerTest {

    @Test
    void awardsAllFivePatterns() {
        MeowPointTracker tracker = MeowPointTracker.createDefault();
        var bus = new io.github.finalwave.model.quest.event.GameEventBus();
        tracker.registerOn(bus);

        bus.publish(new GameEvent.ZombieKilled(
                "ZombieDefault", "Peashooter", "score-game", 5, 1, 1.0, "proj-1", 10));
        bus.publish(new GameEvent.ZombieKilled(
                "ZombieDefault", "Peashooter", "score-game", 6, 1, 1.5, "proj-1", 10));
        bus.publish(new GameEvent.ZombieKilled(
                "ZombieArmor1", "Peashooter", "score-game", 4, 2, 2.0, null, 11));
        bus.publish(new GameEvent.LawnMowerTriggered(2, 3));
        bus.publish(new GameEvent.GameFinished(true, 100, 0, 30));

        MeowPointBreakdown breakdown = tracker.getBreakdown();
        assertEquals(40, breakdown.patternScores().get(PierceMultiKillPattern.ID));
        assertEquals(45, breakdown.patternScores().get(SpeedKillPattern.ID));
        assertEquals(25, breakdown.patternScores().get(SimultaneousKillPattern.ID));
        assertEquals(60, breakdown.patternScores().get(MowerSweepPattern.ID));
        assertEquals(70, breakdown.patternScores().get(EfficientVictoryPattern.ID));
        assertEquals(240, breakdown.total());
    }
}
