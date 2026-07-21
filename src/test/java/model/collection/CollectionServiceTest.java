package model.collection;

import model.definition.PlantRegistry;
import model.user.Gender;
import model.user.UnlockService;
import model.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class CollectionServiceTest {

    private CollectionService service;
    private User user;

    @BeforeEach
    void setUp() throws IOException {
        PlantRegistry registry = new PlantRegistry();
        registry.loadFromJson("src/main/resources/plants.json");
        service = CollectionService.createDefault(registry);
        user = new User("tester", "hash", "Tester", "test@example.com", Gender.MALE);
        user.setCoins(10_000);
    }

    @Test
    void formatOwnedPlantsListsUnlockedStarterPlants() {
        var lines = service.formatOwnedPlants(user);
        assertFalse(lines.isEmpty());
        assertTrue(lines.stream().anyMatch(line -> line.contains("Peashooter")));
        assertTrue(lines.stream().anyMatch(line -> line.contains("Sunflower")));
        assertTrue(lines.stream().anyMatch(line -> line.contains("Wall-nut")));
    }

    @Test
    void formatAllPlantsMarksLockedAndOwnedPlants() {
        var lines = service.formatAllPlants(user);
        assertTrue(lines.stream().anyMatch(line -> line.contains("Peashooter") && line.contains("OWNED")));
        assertTrue(lines.stream().anyMatch(line -> line.contains("Cherry Bomb") && line.contains("LOCKED")));
    }

    @Test
    void formatPlantDetailsShowsTextStatsForOwnedPlant() {
        String details = service.formatPlantDetails(user, "Peashooter");
        assertNotNull(details);
        assertTrue(details.contains("Plant: Peashooter"));
        assertTrue(details.contains("Category: SHOOTER"));
        assertTrue(details.contains("Owned: true"));
        assertTrue(details.contains("Damage:"));
    }

    @Test
    void formatPlantDetailsShowsLockedPlantAsNotOwned() {
        String details = service.formatPlantDetails(user, "Cherry Bomb");
        assertNotNull(details);
        assertTrue(details.contains("Owned: false"));
    }

    @Test
    void upgradeReportsInsufficientCoins() {
        user.setCoins(0);
        var result = service.upgradePlant(user, "Peashooter");
        assertFalse(result.success());
        assertEquals(PlantCollection.UpgradeFailure.INSUFFICIENT_COINS, result.failure());
    }

    @Test
    void upgradeReportsInsufficientSeedPackets() {
        var result = service.upgradePlant(user, "Peashooter");
        assertFalse(result.success());
        assertEquals(PlantCollection.UpgradeFailure.INSUFFICIENT_SEED_PACKETS, result.failure());
    }

    @Test
    void upgradeSuccessDeductsCoinsFromUser() {
        user.getPlantProgress().addSeedPackets("Peashooter", 20);
        int coinsBefore = user.getCoins();
        var result = service.upgradePlant(user, "Peashooter");
        assertTrue(result.success());
        assertEquals(2, result.newLevel());
        assertTrue(user.getCoins() < coinsBefore);
        assertEquals(2, user.getPlantProgress().getOwnedPlant("Peashooter").orElseThrow().getLevel());
    }

    @Test
    void purchaseDeductsCoinsAndUnlocksPlantWithNews() {
        UnlockService unlockService = new UnlockService();
        var result = service.purchasePlant(user, unlockService, "Cherry Bomb");
        assertTrue(result.success());
        assertTrue(result.newlyUnlocked());
        assertTrue(user.getPlantProgress().isOwned("Cherry Bomb"));
        assertEquals(8000, user.getCoins());
        assertEquals(1, user.getNewsItems().size());
    }

    @Test
    void purchaseFailsWhenAlreadyOwned() {
        UnlockService unlockService = new UnlockService();
        assertTrue(service.purchasePlant(user, unlockService, "Cherry Bomb").success());
        var second = service.purchasePlant(user, unlockService, "Cherry Bomb");
        assertFalse(second.success());
        assertEquals(PlantCollection.PurchaseFailure.ALREADY_OWNED, second.failure());
    }

    @Test
    void purchaseFailsWithInsufficientCoins() {
        user.setCoins(500);
        UnlockService unlockService = new UnlockService();
        var result = service.purchasePlant(user, unlockService, "Cherry Bomb");
        assertFalse(result.success());
        assertEquals(PlantCollection.PurchaseFailure.INSUFFICIENT_COINS, result.failure());
        assertFalse(user.getPlantProgress().isOwned("Cherry Bomb"));
    }

    @Test
    void purchaseFailsForUnknownPlant() {
        UnlockService unlockService = new UnlockService();
        var result = service.purchasePlant(user, unlockService, "Unknown Plant");
        assertFalse(result.success());
        assertEquals(PlantCollection.PurchaseFailure.UNKNOWN_PLANT, result.failure());
    }

    @Test
    void formatSeenZombiesEmptyWhenNoneSeen() {
        var lines = service.formatSeenZombies(user);
        assertEquals(1, lines.size());
        assertTrue(lines.get(0).contains("No zombies seen yet"));
    }

    @Test
    void formatSeenZombiesListsUnlockedZombies() {
        user.getUnlockedZombies().add("ZombieDefault");
        var lines = service.formatSeenZombies(user);
        assertTrue(lines.contains("ZombieDefault"));
    }

    @Test
    void formatAllZombiesShowsEmptySlotsForUnseen() {
        var lines = service.formatAllZombies(user);
        assertFalse(lines.isEmpty());
        assertTrue(lines.stream().anyMatch(line -> line.contains("[ empty ]")));
        user.getUnlockedZombies().add("ZombieDefault");
        var updated = service.formatAllZombies(user);
        assertTrue(updated.stream().anyMatch(line -> line.contains("ZombieDefault | SEEN")));
    }

    @Test
    void formatZombieDetailsRequiresSeenZombie() {
        assertNull(service.formatZombieDetails(user, "ZombieDefault"));
        user.getUnlockedZombies().add("ZombieDefault");
        String details = service.formatZombieDetails(user, "ZombieDefault");
        assertNotNull(details);
        assertTrue(details.contains("Zombie: ZombieDefault"));
        assertTrue(details.contains("Hitpoints:"));
    }

    @Test
    void isKnownPlantAndZombieLookup() {
        assertTrue(service.isKnownPlant("Peashooter"));
        assertFalse(service.isKnownPlant("Fake Plant"));
        assertTrue(service.isKnownZombie("ZombieDefault"));
        assertFalse(service.isKnownZombie("Fake Zombie"));
    }
}
