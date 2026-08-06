package io.github.finalwave.model.user;

import io.github.finalwave.model.collection.CollectionService;
import io.github.finalwave.model.collection.PlantCollection;
import io.github.finalwave.model.definition.PlantRegistry;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserDatabaseCollectionProgressTest {

    private static final Path DATABASE = Path.of("target", "collection-progress-test.db");
    private static CollectionService collectionService;

    @BeforeAll
    static void configureDatabase() throws Exception {
        Files.createDirectories(DATABASE.getParent());
        Files.deleteIfExists(DATABASE);
        System.setProperty("pvz.database.url", "jdbc:sqlite:" + DATABASE.toAbsolutePath());
        UserDatabase.resetInstanceForTests();

        PlantRegistry registry = new PlantRegistry();
        registry.loadFromJson("src/main/resources/plants.json");
        collectionService = CollectionService.createDefault(registry);
    }

    @AfterAll
    static void cleanUpDatabase() throws Exception {
        UserDatabase.resetInstanceForTests();
        System.clearProperty("pvz.database.url");
        Files.deleteIfExists(DATABASE);
    }

    @Test
    @Order(1)
    void purchasePersistsCoinsPlantUnlockAndNews() {
        UserDatabase database = UserDatabase.getInstance();
        User user = registerUser(database, "collection-buyer");
        user.setCoins(PlantCollection.PURCHASE_COST_COINS);
        database.saveUserWallet(user);

        UnlockService unlockService = new UnlockService();
        PlantCollection.PurchaseResult result = collectionService.purchasePlant(
                user, unlockService, "Cherry Bomb");
        assertTrue(result.success());
        database.saveUserWallet(user);
        database.savePlantProgress(user);
        database.saveUserNews(user);

        User loaded = database.getUser("collection-buyer");
        assertTrue(loaded.getPlantProgress().isOwned("Cherry Bomb"));
        assertEquals(0, loaded.getCoins());
        assertEquals(1, loaded.getNewsItems().size());
    }

    @Test
    @Order(2)
    void upgradePersistsLevelSeedDeductionAndCoins() throws IOException {
        UserDatabase database = UserDatabase.getInstance();
        User user = registerUser(database, "collection-upgrader");
        user.setCoins(10_000);
        user.getPlantProgress().addSeedPackets("Peashooter", 20);
        database.saveUserWallet(user);
        database.savePlantProgress(user);

        PlantCollection.UpgradeResult result = collectionService.upgradePlant(user, "Peashooter");
        assertTrue(result.success());
        database.saveUserWallet(user);
        database.savePlantProgress(user);

        User loaded = database.getUser("collection-upgrader");
        assertEquals(2, loaded.getPlantProgress().getOwnedPlant("Peashooter").orElseThrow().getLevel());
        assertTrue(loaded.getCoins() < 10_000);
        assertTrue(loaded.getPlantProgress().getOwnedPlant("Peashooter").orElseThrow().getSeedPackets() < 20);
    }

    @Test
    @Order(3)
    void unlockedZombiesPersistAcrossSessions() {
        UserDatabase database = UserDatabase.getInstance();
        User user = registerUser(database, "collection-zombie-viewer");

        UnlockService unlockService = new UnlockService();
        assertTrue(unlockService.unlockZombie(user, "ZombieDefault"));
        database.saveUserWallet(user);

        User loaded = database.getUser("collection-zombie-viewer");
        assertTrue(loaded.getUnlockedZombies().contains("ZombieDefault"));

        assertNotNull(collectionService.formatZombieDetails(loaded, "ZombieDefault"));
        var allZombies = collectionService.formatAllZombies(loaded);
        assertTrue(allZombies.stream().anyMatch(line -> line.contains("ZombieDefault | SEEN")));
    }

    private static User registerUser(UserDatabase database, String username) {
        User user = new User(
                username,
                "password-hash",
                "Collection Tester",
                username + "@example.com",
                Gender.MALE);
        database.registerUser(user);
        return database.getUser(username);
    }
}
