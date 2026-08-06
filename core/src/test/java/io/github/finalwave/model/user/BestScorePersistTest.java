package io.github.finalwave.model.user;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import io.github.finalwave.util.HashUtil;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BestScorePersistTest {

    private static final Path DATABASE = Path.of("target", "best-score-progress.db");

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
    void persistsBestMeowPointAndOnlyRaises() {
        User user = new User("score-user", HashUtil.hashSHA256("Passw0rd!"),
                "SU", "score@example.com", Gender.MALE);
        user.setSecurityQuestionId(1);
        user.setSecurityAnswerHash(HashUtil.hashSHA256("a"));
        UserDatabase.getInstance().registerUser(user);

        assertTrue(user.updateBestMeowPoint(150));
        UserDatabase.getInstance().saveBestMeowPoint(user);
        assertFalse(user.updateBestMeowPoint(100));
        assertEquals(150, user.getBestMeowPoint());

        UserDatabase.resetInstanceForTests();
        User reloaded = UserDatabase.getInstance().getUser("score-user");
        assertEquals(150, reloaded.getBestMeowPoint());

        assertTrue(reloaded.updateBestMeowPoint(200));
        UserDatabase.getInstance().saveBestMeowPoint(reloaded);

        UserDatabase.resetInstanceForTests();
        User again = UserDatabase.getInstance().getUser("score-user");
        assertEquals(200, again.getBestMeowPoint());
    }
}
