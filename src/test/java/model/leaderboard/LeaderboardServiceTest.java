package model.leaderboard;

import model.adventure.ChapterId;
import model.minigame.MiniGameId;
import model.quest.Quest;
import model.quest.QuestTracker;
import model.user.Gender;
import model.user.User;
import org.junit.jupiter.api.Test;
import util.HashUtil;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LeaderboardServiceTest {

    @Test
    void buildAggregatesProgressMinigamesQuestsAndScore() {
        User alice = user("alice");
        alice.getChapterProgress().markLevelCompleted(ChapterId.ANCIENT_EGYPT, 1);
        alice.getChapterProgress().markLevelCompleted(ChapterId.FROSTBITE_CAVES, 2);
        alice.getMiniGameProgress().markStageCompleted(MiniGameId.VASE_BREAKER, 1);
        alice.getMiniGameProgress().markStageCompleted(MiniGameId.VASE_BREAKER, 2);
        alice.setBestMeioPoint(120);
        QuestTracker tracker = new QuestTracker(q -> {
        });
        tracker.setQuests(List.of(
                completedQuest("d1", Quest.Category.DAILY),
                completedQuest("m1", Quest.Category.MAIN),
                completedQuest("e1", Quest.Category.EPIC_CHALLENGE),
                openQuest("d2", Quest.Category.DAILY)));
        alice.setQuestTracker(tracker);

        List<LeaderboardEntry> entries = LeaderboardService.build(List.of(alice));
        assertEquals(1, entries.size());
        LeaderboardEntry entry = entries.getFirst();
        assertEquals("alice", entry.username());
        assertEquals("Level 2 Chapter 2", entry.progressLabel());
        assertEquals(2, entry.minigameCount());
        assertEquals(1, entry.dailyQuestCount());
        assertEquals(2, entry.nonDailyQuestCount());
        assertEquals(120, entry.bestScore());
    }

    @Test
    void sortByBestScoreDescending() {
        User low = user("low");
        low.setBestMeioPoint(10);
        low.setQuestTracker(emptyTracker());
        User high = user("high");
        high.setBestMeioPoint(99);
        high.setQuestTracker(emptyTracker());

        List<LeaderboardEntry> sorted = LeaderboardService.sort(
                LeaderboardService.build(List.of(low, high)),
                LeaderboardSortColumn.BEST_SCORE,
                false);

        assertEquals("high", sorted.get(0).username());
        assertEquals("low", sorted.get(1).username());
    }

    @Test
    void furthestProgressUsesHigherChapterOverHigherLevel() {
        User user = user("progress");
        user.getChapterProgress().markLevelCompleted(ChapterId.ANCIENT_EGYPT, 4);
        user.getChapterProgress().markLevelCompleted(ChapterId.BIG_WAVE_BEACH, 1);
        user.setQuestTracker(emptyTracker());

        LeaderboardEntry entry = LeaderboardService.build(List.of(user)).getFirst();
        assertEquals("Level 1 Chapter 3", entry.progressLabel());
        assertTrue(entry.progressSortKey() > ChapterId.ANCIENT_EGYPT.ordinal() * 100 + 4);
    }

    @Test
    void emptyProgressShowsDash() {
        User user = user("newbie");
        user.setQuestTracker(emptyTracker());
        LeaderboardEntry entry = LeaderboardService.build(List.of(user)).getFirst();
        assertEquals("-", entry.progressLabel());
        assertEquals(-1, entry.progressSortKey());
    }

    private static User user(String username) {
        return new User(
                username,
                HashUtil.hashSHA256("Passw0rd!"),
                username,
                username + "@example.com",
                Gender.MALE);
    }

    private static QuestTracker emptyTracker() {
        QuestTracker tracker = new QuestTracker(q -> {
        });
        tracker.setQuests(List.of());
        return tracker;
    }

    private static Quest completedQuest(String id, Quest.Category category) {
        Quest quest = openQuest(id, category);
        quest.restoreState(true, false, null);
        return quest;
    }

    private static Quest openQuest(String id, Quest.Category category) {
        return new Quest(
                id,
                id,
                category,
                Quest.Priority.MEDIUM,
                noopCondition(),
                model.quest.reward.QuestReward.coins(0));
    }

    private static model.quest.condition.QuestCondition noopCondition() {
        return new model.quest.condition.QuestCondition() {
            @Override
            public void onEvent(model.quest.event.GameEvent event) {
            }

            @Override
            public boolean isMet() {
                return false;
            }

            @Override
            public void reset() {
            }

            @Override
            public String describe() {
                return "";
            }
        };
    }
}
