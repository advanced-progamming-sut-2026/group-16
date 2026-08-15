package io.github.finalwave.model.quest;

import io.github.finalwave.model.App;
import io.github.finalwave.model.collection.PlayerPlantProgress;
import io.github.finalwave.model.quest.event.GameEvent;
import io.github.finalwave.model.quest.event.GameEventBus;
import io.github.finalwave.model.quest.reward.QuestReward;
import io.github.finalwave.model.user.Gender;
import io.github.finalwave.model.user.User;
import io.github.finalwave.model.user.UserDatabase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import io.github.finalwave.util.HashUtil;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuestRewardPersistTest {

    private static final Path DATABASE = Path.of("target", "quest-reward-persist.db");

    @BeforeEach
    void setUp() throws Exception {
        Files.createDirectories(DATABASE.getParent());
        Files.deleteIfExists(DATABASE);
        System.setProperty("pvz.database.url", "jdbc:sqlite:" + DATABASE.toAbsolutePath());
        UserDatabase.resetInstanceForTests();
    }

    @AfterEach
    void tearDown() throws Exception {
        UserDatabase.resetInstanceForTests();
        System.clearProperty("pvz.database.url");
        Files.deleteIfExists(DATABASE);
    }

    @Test
    void appliesDiamondsAndPersistsCompletedQuest() {
        User user = new User("reward-user", HashUtil.hashSHA256("Passw0rd!"),
                "RU", "reward@example.com", Gender.MALE);
        user.setSecurityQuestionId(1);
        user.setSecurityAnswerHash(HashUtil.hashSHA256("a"));
        UserDatabase.getInstance().registerUser(user);

        int diamondsBefore = user.getDiamonds();
        QuestService.applyReward(user, QuestReward.diamonds(20));
        assertEquals(diamondsBefore + 20, user.getDiamonds());

        QuestTracker tracker = QuestService.createTrackerFor(user, null);
        Quest quest = tracker.getDailyQuests().stream()
                .filter(q -> q.getId().equals("daily_sunblock"))
                .findFirst()
                .orElseThrow();
        quest.restoreState(true, true, quest.exportProgressBlob());
        UserDatabase.getInstance().saveQuestProgress(user);

        UserDatabase.resetInstanceForTests();
        User reloaded = UserDatabase.getInstance().getUser("reward-user");
        QuestTracker reloadedTracker = QuestService.createTrackerFor(reloaded, null);
        Quest restored = reloadedTracker.getQuests().stream()
                .filter(q -> q.getId().equals("daily_sunblock"))
                .findFirst()
                .orElseThrow();
        assertTrue(restored.isCompleted());
        assertTrue(restored.isRewardClaimed());
        assertEquals(20, reloaded.getDiamonds());
    }

    @Test
    void randomPlantUnlockPublishesNews() {
        User user = new User("unlock-news", HashUtil.hashSHA256("Passw0rd!"),
                "UN", "unlock-news@example.com", Gender.MALE);
        user.setSecurityQuestionId(1);
        user.setSecurityAnswerHash(HashUtil.hashSHA256("a"));
        UserDatabase.getInstance().registerUser(user);

        int newsBefore = user.getNewsItems().size();
        QuestService.applyReward(user, QuestReward.randomPlantUnlock());
        assertTrue(user.getNewsItems().size() > newsBefore);
        assertTrue(user.hasUnreadNews());
    }

    @Test
    void completedQuestWaitsForOneManualClaimAndPersistsIt() {
        User user = registeredUser("manual-claim");
        QuestTracker tracker = QuestService.createTrackerFor(user, null);
        Quest quest = tracker.getDailyQuests().stream()
                .filter(candidate -> candidate.getId().equals("daily_sunblock"))
                .findFirst()
                .orElseThrow();
        GameEventBus bus = new GameEventBus();
        tracker.registerOn(bus);
        int coinsBefore = user.getCoins();

        bus.publish(new GameEvent.SunCollected(5000));

        assertTrue(quest.isCompleted());
        assertFalse(quest.isRewardClaimed());
        assertEquals(coinsBefore, user.getCoins());
        assertTrue(QuestService.claimReward(user, quest));
        assertTrue(quest.isRewardClaimed());
        assertEquals(coinsBefore + quest.getReward().getCoins(), user.getCoins());
        assertFalse(QuestService.claimReward(user, quest));
        assertEquals(coinsBefore + quest.getReward().getCoins(), user.getCoins());

        UserDatabase.resetInstanceForTests();
        User reloaded = UserDatabase.getInstance().getUser("manual-claim");
        Quest restored = QuestService.createTrackerFor(reloaded, null).getQuests().stream()
                .filter(candidate -> candidate.getId().equals("daily_sunblock"))
                .findFirst()
                .orElseThrow();
        assertTrue(restored.isCompleted());
        assertTrue(restored.isRewardClaimed());
    }

    @Test
    void claimAllOnlyClaimsTheRequestedCategory() {
        User user = registeredUser("claim-all");
        QuestTracker tracker = QuestService.createTrackerFor(user, null);
        Quest firstDaily = tracker.getDailyQuests().get(0);
        Quest secondDaily = tracker.getDailyQuests().get(1);
        Quest main = tracker.getMainQuests().get(0);
        firstDaily.restoreState(true, false, firstDaily.exportProgressBlob());
        secondDaily.restoreState(true, false, secondDaily.exportProgressBlob());
        main.restoreState(true, false, main.exportProgressBlob());

        int claimed = QuestService.claimAll(user, tracker.getDailyQuests());

        assertEquals(2, claimed);
        assertTrue(firstDaily.isRewardClaimed());
        assertTrue(secondDaily.isRewardClaimed());
        assertFalse(main.isRewardClaimed());
        assertEquals(0, QuestService.claimAll(user, tracker.getDailyQuests()));
    }

    private static User registeredUser(String username) {
        User user = new User(username, HashUtil.hashSHA256("Passw0rd!"),
                username, username + "@example.com", Gender.MALE);
        user.setSecurityQuestionId(1);
        user.setSecurityAnswerHash(HashUtil.hashSHA256("a"));
        UserDatabase.getInstance().registerUser(user);
        return user;
    }
}
