package model.user;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import util.database.DatabaseUtil;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserDatabasePlantProgressTest {

    private static final Path DATABASE = Path.of("target", "plant-progress-test.db");

    @BeforeAll
    static void configureDatabase() throws Exception {
        Files.deleteIfExists(DATABASE);
        System.setProperty("pvz.database.url", "jdbc:sqlite:" + DATABASE);
    }

    @AfterAll
    static void cleanUpDatabase() throws Exception {
        System.clearProperty("pvz.database.url");
        Files.deleteIfExists(DATABASE);
    }

    @Test
    void savesEachOwnedPlantAsASqliteRow() throws Exception {
        UserDatabase database = UserDatabase.getInstance();
        User user = new User(
                "plant-owner",
                "password-hash",
                "Plant Owner",
                "plants@example.com",
                Gender.MALE);
        user.getPlantProgress().unlock("Cherry Bomb");
        database.registerUser(user);

        User loaded = database.getUser("plant-owner");
        assertTrue(loaded.getPlantProgress().isOwned("Peashooter"));
        assertTrue(loaded.getPlantProgress().isOwned("Cherry Bomb"));

        loaded.getPlantProgress().unlock("Repeater");
        database.savePlantProgress(loaded);

        User reloaded = database.getUser("plant-owner");
        assertTrue(reloaded.getPlantProgress().isOwned("Repeater"));

        try (var connection = DatabaseUtil.getConnection();
             var statement = connection.prepareStatement(
                     "SELECT COUNT(*) FROM user_plants WHERE userId = ?")) {
            statement.setLong(1, user.getId());
            try (var rows = statement.executeQuery()) {
                assertEquals(5, rows.getInt(1));
            }
        }
    }
}
