package io.github.finalwave.profile;

import io.github.finalwave.model.user.Gender;
import io.github.finalwave.model.user.User;
import io.github.finalwave.model.user.UserDatabase;
import io.github.finalwave.network.auth.LoginOkPayload;
import io.github.finalwave.util.HashUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ProfileApplierTest {
    private static final Path DATABASE = Path.of("target", "profile-applier-test.db");

    private UserDatabase database;

    @BeforeEach
    void setUp() throws Exception {
        Files.createDirectories(DATABASE.getParent());
        Files.deleteIfExists(DATABASE);
        System.setProperty("pvz.database.url", "jdbc:sqlite:" + DATABASE.toAbsolutePath());
        UserDatabase.resetInstanceForTests();
        database = UserDatabase.getInstance();
    }

    @AfterEach
    void tearDown() throws Exception {
        UserDatabase.resetInstanceForTests();
        System.clearProperty("pvz.database.url");
        Files.deleteIfExists(DATABASE);
    }

    @Test
    void exportThenApplyRoundTripMatchesWalletAndUnlocks() {
        User original = new User(
                "profile-user",
                HashUtil.hashSHA256("Password1!"),
                "ProfileNick",
                "profile@example.com",
                Gender.MALE
        );
        database.registerUser(original);

        User loaded = database.getUser("profile-user");
        assertNotNull(loaded);
        loaded.setCoins(250);
        loaded.setDiamonds(12);
        loaded.setPlantFood(3);
        database.saveUserWallet(loaded);

        LoginOkPayload exported = ProfileExporter.export(loaded, database);
        User applied = ProfileApplier.apply(exported);

        assertEquals(loaded.getId(), applied.getId());
        assertEquals("profile-user", applied.getUsername());
        assertEquals("ProfileNick", applied.getNickname());
        assertEquals("profile@example.com", applied.getEmail());
        assertEquals(250, applied.getCoins());
        assertEquals(12, applied.getDiamonds());
        assertEquals(3, applied.getPlantFood());
        assertEquals(loaded.getUnlockedLevels().size(), applied.getUnlockedLevels().size());
        assertEquals(loaded.getPlantProgress().getOwnedPlants().size(),
                applied.getPlantProgress().getOwnedPlants().size());
    }
}
