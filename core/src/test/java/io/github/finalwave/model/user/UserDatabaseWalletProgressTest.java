package io.github.finalwave.model.user;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserDatabaseWalletProgressTest {
    private static final Path DATABASE = Path.of("target", "wallet-progress-test.db");

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
    @Order(1)
    void registerInitializesWalletAndDefaultGreenhouse() {
        UserDatabase database = UserDatabase.getInstance();
        User user = new User(
                "wallet-owner",
                "password-hash",
                "Wallet Owner",
                "wallet@example.com",
                Gender.FEMALE);
        database.registerUser(user);
        assertTrue(user.getId() > 0);

        User loaded = database.getUser("wallet-owner");
        assertNotNull(loaded);
        assertEquals(0, loaded.getCoins());
        assertEquals(0, loaded.getDiamonds());
        assertEquals(0, loaded.getPlantFood());
        assertEquals(12, loaded.getGreenhousePots().size());
        assertEquals(4, loaded.countUnlockedPots());
        assertNotNull(loaded.getPotAt(1, 1));
        assertFalse(loaded.getPotAt(1, 1).isLocked());
        assertTrue(loaded.getPotAt(1, 2).isLocked());
    }

    @Test
    @Order(2)
    void saveAndLoadWalletGreenhouseBoostsAndDailyOffer() {
        UserDatabase database = UserDatabase.getInstance();
        User user = new User(
                "wallet-updated",
                "password-hash",
                "Wallet Updated",
                "wallet2@example.com",
                Gender.MALE);
        database.registerUser(user);
        assertTrue(user.getId() > 0);
        assertEquals(12, user.getGreenhousePots().size());

        user.setCoins(2500);
        user.setDiamonds(8);
        user.setPlantFood(2);
        user.getStoredBoosts().add("Sunflower");
        user.getPotAt(1, 1).plant(GreenhousePot.MARIGOLD, true, 123456789L);
        user.getPotAt(1, 2).setLocked(false);
        user.setDailyOfferPlant("Peashooter");
        user.setDailyOfferDate(LocalDate.of(2026, 7, 16));
        user.setDailyOfferPurchased(true);
        database.saveUserWallet(user);

        User loaded = database.getUser("wallet-updated");
        assertNotNull(loaded);
        assertEquals(2500, loaded.getCoins());
        assertEquals(8, loaded.getDiamonds());
        assertEquals(2, loaded.getPlantFood());
        assertTrue(loaded.hasStoredBoost("Sunflower"));
        assertNotNull(loaded.getPotAt(1, 1));
        assertFalse(loaded.getPotAt(1, 1).isEmpty());
        assertEquals(GreenhousePot.MARIGOLD, loaded.getPotAt(1, 1).getPlantType());
        assertEquals(123456789L, loaded.getPotAt(1, 1).getPlantedAtMillis());
        assertFalse(loaded.getPotAt(1, 2).isLocked());
        assertEquals("Peashooter", loaded.getDailyOfferPlant());
        assertEquals(LocalDate.of(2026, 7, 16), loaded.getDailyOfferDate());
        assertTrue(loaded.isDailyOfferPurchased());
    }
}
