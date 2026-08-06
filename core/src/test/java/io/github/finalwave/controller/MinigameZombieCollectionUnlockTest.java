package io.github.finalwave.controller;

import io.github.finalwave.model.definition.PlantRegistry;
import io.github.finalwave.model.definition.ZombieRegistry;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.minigame.MiniGameStageConfig;
import io.github.finalwave.model.minigame.mode.WalnutBowlingMode;
import io.github.finalwave.model.user.Gender;
import io.github.finalwave.model.user.User;
import io.github.finalwave.model.user.UserDatabase;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MinigameZombieCollectionUnlockTest {

    private static final Path DATABASE = Path.of("target", "minigame-zombie-unlock-test.db");

    @BeforeAll
    static void configureDatabase() throws Exception {
        Files.createDirectories(DATABASE.getParent());
        Files.deleteIfExists(DATABASE);
        System.setProperty("pvz.database.url", "jdbc:sqlite:" + DATABASE.toAbsolutePath());
        UserDatabase.resetInstanceForTests();
    }

    @AfterAll
    static void cleanUpDatabase() throws Exception {
        UserDatabase.resetInstanceForTests();
        System.clearProperty("pvz.database.url");
        Files.deleteIfExists(DATABASE);
    }

    @Test
    void walnutBowlingSpawnUnlocksZombieForCollection() throws Exception {
        UserDatabase database = UserDatabase.getInstance();
        User user = new User(
                "mg-zombie-unlock",
                "password-hash",
                "MG Unlock",
                "mg-zombie-unlock@example.com",
                Gender.MALE);
        database.registerUser(user);
        assertFalse(user.getUnlockedZombies().contains("ZombieDefault"));

        PlantRegistry plantRegistry = new PlantRegistry();
        plantRegistry.loadFromJson("src/main/resources/plants.json");
        ZombieRegistry zombieRegistry = new ZombieRegistry();
        zombieRegistry.loadFromJson("src/main/resources/zombies.json");
        zombieRegistry.loadArmorFromJson("src/main/resources/ArmorTypeData.json");

        MiniGameStageConfig stage = MiniGameStageConfig.walnutBowling(1);
        WalnutBowlingMode mode = new WalnutBowlingMode(stage, plantRegistry, zombieRegistry, new Random(1));
        GameSession session = mode.createSession();
        MiniGameHubController hub = new MiniGameHubController(user, database, null);
        WalnutBowlingController controller = new WalnutBowlingController(
                user, database, hub, mode, session, stage);

        controller.onZombieSpawned("ZombieDefault", 1, 1, 100);
        assertTrue(user.getUnlockedZombies().contains("ZombieDefault"));
        assertEquals(1, user.getNewsItems().size());

        controller.onZombieSpawned("ZombieDefault", 1, 1, 100);
        assertEquals(1, user.getUnlockedZombies().stream().filter("ZombieDefault"::equals).count());
        assertEquals(1, user.getNewsItems().size());

        UserDatabase.resetInstanceForTests();
        User reloaded = UserDatabase.getInstance().getUser("mg-zombie-unlock");
        assertTrue(reloaded.getUnlockedZombies().contains("ZombieDefault"));
    }
}
