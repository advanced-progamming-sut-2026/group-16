package model.quest;

import model.App;
import model.collection.PlayerPlantProgress;
import model.quest.reward.QuestReward;
import model.user.Gender;
import model.user.User;
import model.user.UserDatabase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.HashUtil;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
                .filter(q -> q.getId().equals("daily_sun_3000"))
                .findFirst()
                .orElseThrow();
        quest.restoreState(true, true, "3000");
        UserDatabase.getInstance().saveQuestProgress(user);

        UserDatabase.resetInstanceForTests();
        User reloaded = UserDatabase.getInstance().getUser("reward-user");
        QuestTracker reloadedTracker = QuestService.createTrackerFor(reloaded, null);
        Quest restored = reloadedTracker.getQuests().stream()
                .filter(q -> q.getId().equals("daily_sun_3000"))
                .findFirst()
                .orElseThrow();
        assertTrue(restored.isCompleted());
        assertTrue(restored.isRewardClaimed());
        assertEquals(20, reloaded.getDiamonds());
    }

    @Test
    void anySeedPacketResolvesToUnlockedPlant() {
        User user = new User("seed-user", HashUtil.hashSHA256("Passw0rd!"),
                "SU", "seed@example.com", Gender.MALE);
        user.setSecurityQuestionId(1);
        user.setSecurityAnswerHash(HashUtil.hashSHA256("a"));
        UserDatabase.getInstance().registerUser(user);

        // Restrict unlocked set to a single plant so ANY is deterministic.
        user.setPlantProgress(PlayerPlantProgress.fromOwnedPlants(
                java.util.List.of(new model.collection.OwnedPlant("Peashooter", 1, true, 0))));
        int before = user.getPlantProgress().getOwnedPlant("Peashooter").orElseThrow().getSeedPackets();

        QuestService.applyReward(user, QuestReward.seedPackets("ANY", 5));

        assertEquals(before + 5,
                user.getPlantProgress().getOwnedPlant("Peashooter").orElseThrow().getSeedPackets());
    }
}
