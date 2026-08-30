package io.github.finalwave.debug;

import io.github.finalwave.model.user.Gender;
import io.github.finalwave.model.user.User;
import io.github.finalwave.model.user.UserDatabase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DebugCheatPersistenceTest {
    private static final Path DATABASE = Path.of("target", "debug-cheat-persistence-test.db");

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
    void rejectsCheatsWhenDebugModeOff() {
        UserDatabase database = UserDatabase.getInstance();
        User user = registerUser(database, "cheater-a");
        user.setDebugMode(false);
        assertFalse(DebugCheatPersistence.addCoins(user, database, 100));
        assertEquals(0, user.getCoins());
    }

    @Test
    void addsCoinsAndPersists() {
        UserDatabase database = UserDatabase.getInstance();
        User user = registerUser(database, "cheater-b");
        user.setDebugMode(true);
        assertTrue(DebugCheatPersistence.addCoins(user, database, 250));
        assertEquals(250, user.getCoins());
        assertEquals(250, database.getUser("cheater-b").getCoins());
    }

    @Test
    void capsStoredPlantFood() {
        UserDatabase database = UserDatabase.getInstance();
        User user = registerUser(database, "cheater-c");
        user.setDebugMode(true);
        user.setPlantFood(2);
        assertTrue(DebugCheatPersistence.addPlantFood(user, database, 5));
        assertEquals(3, user.getPlantFood());
        assertFalse(DebugCheatPersistence.addPlantFood(user, database, 1));
    }

    private static User registerUser(UserDatabase database, String username) {
        User user = new User(username, "hash", "Cheater", username + "@test.com", Gender.MALE);
        database.registerUser(user);
        return database.getUser(username);
    }
}
