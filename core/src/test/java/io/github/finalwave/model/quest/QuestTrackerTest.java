package io.github.finalwave.model.quest;

import io.github.finalwave.model.quest.event.GameEvent;
import io.github.finalwave.model.quest.event.GameEventBus;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuestTrackerTest {

    @Test
    void completesSunCollectQuestWhenFedSunCollectedEvents() {
        Quest quest = QuestFactory.createDailySunCollector(100);
        List<Quest> completed = new ArrayList<>();
        QuestTracker tracker = new QuestTracker(completed::add);
        tracker.addQuest(quest);

        GameEventBus bus = new GameEventBus();
        tracker.registerOn(bus);

        bus.publish(new GameEvent.SunCollected(40));
        assertFalse(quest.isCompleted());
        assertTrue(completed.isEmpty());

        bus.publish(new GameEvent.SunCollected(60));
        assertTrue(quest.isCompleted());
        assertEquals(1, completed.size());
        assertEquals(quest.getId(), completed.get(0).getId());
    }

    @Test
    void beginSessionDoesNotWipeWinStreakProgress() {
        Quest streakQuest = QuestFactory.createWinStreak(2);
        AtomicInteger completions = new AtomicInteger();
        QuestTracker tracker = new QuestTracker(q -> completions.incrementAndGet());
        tracker.addQuest(streakQuest);

        GameEventBus bus = new GameEventBus();
        tracker.registerOn(bus);

        bus.publish(new GameEvent.GameFinished(true, 0, 0, 10, 5));
        tracker.beginSession();
        bus.publish(new GameEvent.GameFinished(true, 0, 0, 10, 5));

        assertTrue(streakQuest.isCompleted());
        assertEquals(1, completions.get());
    }

    @Test
    void unregisterStopsReceivingEvents() {
        Quest quest = QuestFactory.createDailySunCollector(50);
        QuestTracker tracker = new QuestTracker(q -> {
        });
        tracker.addQuest(quest);
        GameEventBus bus = new GameEventBus();
        tracker.registerOn(bus);
        tracker.unregister();
        bus.publish(new GameEvent.SunCollected(50));
        assertFalse(quest.isCompleted());
    }
}
