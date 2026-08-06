package io.github.finalwave.model.collection;

import io.github.finalwave.model.definition.PlantRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class PlantCollectionTest {

    private PlantRegistry registry;
    private PlantCollection collection;

    @BeforeEach
    void setUp() throws IOException {
        registry = new PlantRegistry();
        registry.loadFromJson("src/main/resources/plants.json");
        collection = new PlantCollection(registry, new PlayerPlantProgress(), 10_000);
    }

    @Test
    void starterPlantsAreUnlocked() {
        assertTrue(collection.getProgress().isOwned("Peashooter"));
        assertFalse(collection.getOwnedPlants().isEmpty());
    }

    @Test
    void purchaseRequiresCoinsAndNotAlreadyOwned() {
        assertTrue(collection.canPurchase("Cherry Bomb"));
        assertTrue(collection.purchase("Cherry Bomb"));
        assertFalse(collection.canPurchase("Cherry Bomb"));
    }

    @Test
    void upgradeConsumesCoinsAndSeedPackets() {
        collection.addSeedPackets("Peashooter", 20);
        assertTrue(collection.canUpgrade("Peashooter"));
        int coinsBefore = collection.getCoins();
        assertTrue(collection.upgrade("Peashooter"));
        assertTrue(collection.getCoins() < coinsBefore);
        assertEquals(2, collection.getProgress().getOwnedPlant("Peashooter").orElseThrow().getLevel());
    }

    @Test
    void upgradeFailsWithInsufficientResources() {
        PlantCollection poor = new PlantCollection(registry, new PlayerPlantProgress(), 0);
        assertFalse(poor.canUpgrade("Peashooter"));
    }

    @Test
    void cannotUpgradePastMaximumLevel() {
        collection.addSeedPackets("Peashooter", 100);
        assertTrue(collection.upgrade("Peashooter"));
        assertTrue(collection.upgrade("Peashooter"));
        assertTrue(collection.upgrade("Peashooter"));
        assertFalse(collection.canUpgrade("Peashooter"));
    }

    @Test
    void levelUpChangesComputedPlantStats() {
        int baseDamage = collection.showPlantDetails("Peashooter").getDamage();
        collection.addSeedPackets("Peashooter", 20);
        assertTrue(collection.upgrade("Peashooter"));
        assertTrue(collection.showPlantDetails("Peashooter").getDamage() > baseDamage);
    }

    @Test
    void appliesSeedPacketQuestReward() {
        collection.applyQuestReward(
                io.github.finalwave.model.quest.reward.QuestReward.seedPackets("Peashooter", 7));
        assertEquals(7, collection.getProgress().getOwnedPlant("Peashooter")
                .orElseThrow().getSeedPackets());
    }

    @Test
    void progressReconstructsFromDatabaseRows() {
        collection.purchase("Cherry Bomb");
        collection.addSeedPackets("Cherry Bomb", 11);

        PlayerPlantProgress restored = PlayerPlantProgress.fromOwnedPlants(
                collection.getProgress().getOwnedPlants().values());

        OwnedPlant cherry = restored.getOwnedPlant("Cherry Bomb").orElseThrow();
        assertTrue(cherry.isUnlocked());
        assertEquals(11, cherry.getSeedPackets());
    }

    @Test
    void rejectsInvalidEconomyMutations() {
        assertThrows(IllegalArgumentException.class, () -> collection.addCoins(-1));
        assertThrows(IllegalArgumentException.class,
                () -> collection.addSeedPackets("Unknown Plant", 1));
        assertThrows(IllegalArgumentException.class,
                () -> new OwnedPlant("", 1, true, 0));
        assertThrows(IllegalArgumentException.class,
                () -> new OwnedPlant("Peashooter", 5, true, 0));
    }

    @Test
    void detailsIncludeDefinitionMetadata() {
        var details = collection.showPlantDetails("Peashooter");
        assertEquals("Peashooter", details.getPlantName());
        assertEquals("SHOOTER", details.getCategory());
        assertTrue(details.getTags().contains("PEA"));
        assertFalse(details.getNextUpgradeSummary().isBlank());
    }

    @Test
    void purchaseFailureDetectsUnknownAlreadyOwnedAndInsufficientCoins() {
        assertEquals(PlantCollection.PurchaseFailure.UNKNOWN_PLANT,
                collection.getPurchaseFailure("Not A Plant"));
        collection.purchase("Cherry Bomb");
        assertEquals(PlantCollection.PurchaseFailure.ALREADY_OWNED,
                collection.getPurchaseFailure("Cherry Bomb"));
        PlantCollection poor = new PlantCollection(registry, new PlayerPlantProgress(), 100);
        assertEquals(PlantCollection.PurchaseFailure.INSUFFICIENT_COINS,
                poor.getPurchaseFailure("Cherry Bomb"));
    }

    @Test
    void upgradeFailureDetectsAllDocErrorCases() {
        assertEquals(PlantCollection.UpgradeFailure.UNKNOWN_PLANT,
                collection.getUpgradeFailure("Missing Plant"));
        assertEquals(PlantCollection.UpgradeFailure.NOT_OWNED,
                collection.getUpgradeFailure("Cherry Bomb"));
        collection.addSeedPackets("Peashooter", 100);
        while (collection.canUpgrade("Peashooter")) {
            collection.upgrade("Peashooter");
        }
        assertEquals(PlantCollection.UpgradeFailure.MAX_LEVEL,
                collection.getUpgradeFailure("Peashooter"));
    }

    @Test
    void upgradeWithResultSyncsLevelAndCoins() {
        collection.addSeedPackets("Peashooter", 20);
        int coinsBefore = collection.getCoins();
        PlantCollection.UpgradeResult result = collection.upgradeWithResult("Peashooter");
        assertTrue(result.success());
        assertEquals(2, result.newLevel());
        assertNull(result.failure());
        assertTrue(collection.getCoins() < coinsBefore);
    }

    @Test
    void upgradeWithResultReportsInsufficientSeedPackets() {
        PlantCollection rich = new PlantCollection(registry, new PlayerPlantProgress(), 10_000);
        assertEquals(PlantCollection.UpgradeFailure.INSUFFICIENT_SEED_PACKETS,
                rich.getUpgradeFailure("Peashooter"));
    }
}
