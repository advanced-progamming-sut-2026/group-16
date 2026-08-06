package io.github.finalwave.model.user;

import io.github.finalwave.model.adventure.ChapterId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import io.github.finalwave.util.HashUtil;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdventureProgressPersistTest {

    private static final Path DATABASE = Path.of("target", "adventure-progress.db");

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
    void persistsChapterUnlockAndDifficulty() {
        User user = new User("adv-user", HashUtil.hashSHA256("Passw0rd!"),
                "AU", "adv@example.com", Gender.MALE);
        user.setSecurityQuestionId(1);
        user.setSecurityAnswerHash(HashUtil.hashSHA256("a"));
        UserDatabase.getInstance().registerUser(user);

        user.setDifficultyLevel(5);
        user.getChapterProgress().markLevelCompleted(ChapterId.ANCIENT_EGYPT, 1);
        UserDatabase.getInstance().saveAdventureProgress(user);

        UserDatabase.resetInstanceForTests();
        User reloaded = UserDatabase.getInstance().getUser("adv-user");
        assertEquals(5, reloaded.getDifficultyLevel());
        assertTrue(reloaded.getChapterProgress().isChapterUnlocked(ChapterId.FROSTBITE_CAVES));
        assertTrue(reloaded.getChapterProgress().isLevelCompleted(ChapterId.ANCIENT_EGYPT, 1));
    }
}
