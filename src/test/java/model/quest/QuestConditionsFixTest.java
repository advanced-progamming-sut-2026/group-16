package model.quest;

import model.adventure.ChapterId;
import model.game.entity.plant.PlantCategory;
import model.game.board.GameBoard;
import model.quest.condition.QuestConditions;
import model.quest.event.GameEvent;
import model.quest.event.GameEventBus;
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
                condition, model.quest.reward.QuestReward.coins(1));
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
    void factoryIncludesLawnmowerChaptersAndBoardVariants() {
        List<Quest> quests = QuestFactory.createAllQuests();
        assertTrue(quests.stream().anyMatch(q -> q.getId().equals("lawnmower_kills_10")));
        assertTrue(quests.stream().anyMatch(q -> q.getId().equals("lawnmower_kills_50")));
        for (ChapterId id : ChapterId.values()) {
            assertTrue(quests.stream().anyMatch(q -> q.getId().equals("chapter_hunter_" + id.getKey())));
        }
        assertTrue(quests.stream().anyMatch(q -> q.getId().equals("daily_sun_4000")));
        assertTrue(quests.stream().anyMatch(q -> q.getId().equals("empty_col_0")));
        assertTrue(quests.stream().anyMatch(q -> q.getId().equals("empty_col_" + (GameBoard.DEFAULT_COLS - 1))));
        assertTrue(quests.stream().anyMatch(q -> q.getId().equals("empty_row_" + (GameBoard.DEFAULT_ROWS - 1))));
        for (PlantCategory category : PlantCategory.values()) {
            assertTrue(quests.stream().anyMatch(q -> q.getId().equals("family_kills_" + category.name())));
            assertTrue(quests.stream().anyMatch(q -> q.getId().equals("family_banned_" + category.name())));
        }
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
