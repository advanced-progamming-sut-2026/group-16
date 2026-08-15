package io.github.finalwave.model.quest;

import io.github.finalwave.model.quest.condition.QuestConditions;
import io.github.finalwave.model.quest.event.GameEvent;
import io.github.finalwave.model.quest.event.GameEventBus;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuestConditionsFixTest {

    @Test
    void familyOnlyKillsFailsOnWrongFamilyAndCompletesOnCorrectFamilyWin() {
        Quest quest = QuestFactory.createFamilyExclusiveKills("SHOOTER");
        QuestTracker tracker = new QuestTracker(q -> {
        });
        tracker.addQuest(quest);
        GameEventBus bus = new GameEventBus();
        tracker.registerOn(bus);

        bus.publish(new GameEvent.ZombieKilled(
                "Z", "Wall-nut", "WALL_NUT", "ch", 3, 0, 1.0, 1.0, null, 1));
        bus.publish(new GameEvent.GameFinished(true, 0, 0, 10, 3));
        assertFalse(quest.isCompleted());

        Quest ok = QuestFactory.createFamilyExclusiveKills("SHOOTER");
        QuestTracker tracker2 = new QuestTracker(q -> {
        });
        tracker2.addQuest(ok);
        GameEventBus bus2 = new GameEventBus();
        tracker2.registerOn(bus2);
        bus2.publish(new GameEvent.ZombieKilled(
                "Z", "Peashooter", "SHOOTER", "ch", 3, 0, 1.0, 1.0, null, 1));
        bus2.publish(new GameEvent.GameFinished(true, 0, 0, 10, 3));
        assertTrue(ok.isCompleted());
    }

    @Test
    void nearVictoryCountsOnlyAfterMowerGoneFromRow() {
        QuestConditions.KillInFirstColumnNoMowerCondition condition =
                new QuestConditions.KillInFirstColumnNoMowerCondition(2, Set.of(0, 1));
        Quest quest = new Quest("nv", "nv", Quest.Category.DAILY, Quest.Priority.MEDIUM,
                condition, io.github.finalwave.model.quest.reward.QuestReward.coins(1));
        QuestTracker tracker = new QuestTracker(q -> {
        });
        tracker.addQuest(quest);
        GameEventBus bus = new GameEventBus();
        tracker.registerOn(bus);

        bus.publish(new GameEvent.ZombieKilled("Z", "P", "SHOOTER", "ch", 0, 0, 1, 1, null, 1));
        assertFalse(quest.isCompleted());

        bus.publish(new GameEvent.LawnMowerTriggered(0, 1));
        bus.publish(new GameEvent.ZombieKilled("Z", "P", "SHOOTER", "ch", 0, 0, 1, 1, null, 2));
        bus.publish(new GameEvent.ZombieKilled("Z", "P", "SHOOTER", "ch", 0, 0, 1, 1, null, 3));
        assertTrue(quest.isCompleted());
    }

    @Test
    void nightPlantsInDayDoesNotCompleteOnNightLevel() {
        Quest quest = QuestFactory.createNightPlantsInDayLevel();
        QuestTracker tracker = new QuestTracker(q -> {
        });
        tracker.addQuest(quest);
        GameEventBus bus = new GameEventBus();
        tracker.registerOn(bus);

        bus.publish(new GameEvent.GameStarted("L1", "ch", true));
        bus.publish(new GameEvent.GameFinished(true, 0, 0, 10, 3));
        assertFalse(quest.isCompleted());
    }

    @Test
    void nightPlantsInDayCompletesWithOnlyNightPlants() {
        Quest quest = QuestFactory.createNightPlantsInDayLevel();
        QuestTracker tracker = new QuestTracker(q -> {
        });
        tracker.addQuest(quest);
        GameEventBus bus = new GameEventBus();
        tracker.registerOn(bus);

        bus.publish(new GameEvent.GameStarted("L1", "ch", false));
        bus.publish(new GameEvent.PlantPlanted("Puff-shroom", "SUN_PRODUCER", 0, 0, true));
        bus.publish(new GameEvent.GameFinished(true, 0, 0, 10, 3));
        assertTrue(quest.isCompleted());
    }

    @Test
    void winStreakOnlyCountsMaxDifficulty() {
        Quest quest = QuestFactory.createWinStreak(2);
        AtomicInteger completions = new AtomicInteger();
        QuestTracker tracker = new QuestTracker(q -> completions.incrementAndGet());
        tracker.addQuest(quest);
        GameEventBus bus = new GameEventBus();
        tracker.registerOn(bus);

        bus.publish(new GameEvent.GameFinished(true, 0, 0, 10, 3));
        bus.publish(new GameEvent.GameFinished(true, 0, 0, 10, 5));
        assertFalse(quest.isCompleted());
        bus.publish(new GameEvent.GameFinished(true, 0, 0, 10, 5));
        assertTrue(quest.isCompleted());
        assertEquals(1, completions.get());
    }

    @Test
    void speedKillUsesFirstWaveClock() {
        Quest quest = QuestFactory.createSpeedKill(2, 30.0);
        QuestTracker tracker = new QuestTracker(q -> {
        });
        tracker.addQuest(quest);
        GameEventBus bus = new GameEventBus();
        tracker.registerOn(bus);

        bus.publish(new GameEvent.ZombieKilled("Z", "P", "SHOOTER", "ch", 1, 0, 40.0, 5.0, null, 1));
        bus.publish(new GameEvent.ZombieKilled("Z", "P", "SHOOTER", "ch", 1, 0, 50.0, 10.0, null, 2));
        assertTrue(quest.isCompleted());
    }

    @Test
    void factoryCreatesStableGroup20CatalogWithCanonicalCopy() {
        List<Quest> quests = QuestFactory.createAllQuests(42L);
        List<Quest> repeated = QuestFactory.createAllQuests(42L);
        List<String> titles = quests.stream().map(Quest::getTitle).toList();

        assertEquals(20, quests.size());
        assertEquals(20, quests.stream().map(Quest::getId).distinct().count());
        assertEquals(
                quests.stream().map(Quest::getDescription).toList(),
                repeated.stream().map(Quest::getDescription).toList());
        assertTrue(titles.containsAll(List.of(
                "Daily Sunblock",
                "Chapter Hunter",
                "Pro Plant Player",
                "Only Cactus",
                "Economical Herbivore",
                "Defense Master",
                "Quick Reaction",
                "Pro Demolition",
                "Symmetry",
                "Family Massacre",
                "Flourish in Limits",
                "Night or Morning",
                "Win Streak",
                "Almost Won",
                "What OCD?",
                "Cloudy Day",
                "One Column Less",
                "Defenseless Row",
                "Defenseless Cross",
                "Lawnmower Time")));
        Quest dailySunblock = questById(quests, "daily_sunblock");
        Quest lawnmowerTime = questById(quests, "lawnmower_time");
        Quest oneColumnLess = questById(quests, "one_column_less");
        Quest defenselessRow = questById(quests, "defenseless_row");
        Quest defenselessCross = questById(quests, "defenseless_cross");
        Quest economicalHerbivore = questById(quests, "economical_herbivore");
        assertTrue(dailySunblock.getDescription().matches("Collect [345]000 of sun during one day"));
        assertTrue(lawnmowerTime.getDescription().matches(
                "Kill at least (10|20|30|40|50) zombies with lawnmowers"));
        assertTrue(oneColumnLess.getDescription().matches(
                "Win a level without planting in the [1-9] column"));
        assertTrue(defenselessRow.getDescription().matches(
                "Win a level without planting in the [1-5] row"));
        assertTrue(defenselessCross.getDescription().matches(
                "Win a level with the [1-5] row and column empty"));
        assertTrue(economicalHerbivore.getDescription().matches(
                "Win a level losing no more than [0-5] plants"));
        assertEquals(14, quests.stream()
                .filter(quest -> quest.getCategory() == Quest.Category.DAILY)
                .count());
        assertEquals(3, quests.stream()
                .filter(quest -> quest.getCategory() == Quest.Category.MAIN)
                .count());
        assertEquals(3, quests.stream()
                .filter(quest -> quest.getCategory() == Quest.Category.EPIC_CHALLENGE)
                .count());
    }

    private static Quest questById(List<Quest> quests, String id) {
        return quests.stream()
                .filter(quest -> quest.getId().equals(id))
                .findFirst()
                .orElseThrow();
    }

    @Test
    void startSessionResetsLevelScopedButKeepsWinStreak() {
        Quest streak = QuestFactory.createWinStreak(5);
        Quest explosive = QuestFactory.createExplosiveExpert(3);
        QuestTracker tracker = new QuestTracker(q -> {
        });
        tracker.addQuest(streak);
        tracker.addQuest(explosive);
        GameEventBus bus = new GameEventBus();
        tracker.registerOn(bus);

        bus.publish(new GameEvent.GameFinished(true, 0, 0, 10, 5));
        bus.publish(new GameEvent.PlantPlanted("Cherry Bomb", "EXPLOSIVE", 0, 0, false));
        assertEquals("Win 5 consecutive levels at max difficulty (streak: 1)", streak.getProgressDescription());
        assertTrue(explosive.getProgressDescription().contains("planted: 1"));

        tracker.beginSession();
        assertEquals("Win 5 consecutive levels at max difficulty (streak: 1)", streak.getProgressDescription());
        assertTrue(explosive.getProgressDescription().contains("planted: 0"));
    }

    @Test
    void dailyResetClearsCompletedDailyQuest() {
        Quest sun = QuestFactory.createDailySunCollector(100);
        QuestTracker tracker = new QuestTracker(q -> {
        });
        tracker.addQuest(sun);
        GameEventBus bus = new GameEventBus();
        tracker.registerOn(bus);
        bus.publish(new GameEvent.SunCollected(100));
        assertTrue(sun.isCompleted());

        tracker.resetDailyQuests();
        assertFalse(sun.isCompleted());
        assertFalse(sun.isRewardClaimed());
    }
}
