package io.github.finalwave.model.user;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import io.github.finalwave.util.HashUtil;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserDatabaseProfilePersistTest {
    private static final Path DATABASE = Path.of("target", "profile-persist-test.db");

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
    void updateProfilePersistsNicknameEmailAndPassword() {
        UserDatabase database = UserDatabase.getInstance();
        String passwordHash = HashUtil.hashSHA256("OldPass1!");
        User user = new User("profile-owner", passwordHash, "OldNick", "old@example.com", Gender.MALE);
        database.registerUser(user);

        user.setNickname("NewNick");
        user.setEmail("new@example.com");
        String newHash = HashUtil.hashSHA256("NewPass1!");
        user.setPasswordHash(newHash);
        database.updateProfile(user);

        User loaded = database.getUser("profile-owner");
        assertNotNull(loaded);
        assertEquals("NewNick", loaded.getNickname());
        assertEquals("new@example.com", loaded.getEmail());
        assertEquals(newHash, loaded.getPasswordHash());
    }

    @Test
    @Order(2)
    void updateProfileRenamesUsernameAndKeepsSameId() {
        UserDatabase database = UserDatabase.getInstance();
        User user = database.getUser("profile-owner");
        assertNotNull(user);
        long id = user.getId();

        user.setUsername("profile-renamed");
        database.updateProfile(user);

        assertNull(database.getUser("profile-owner"));
        User loaded = database.getUser("profile-renamed");
        assertNotNull(loaded);
        assertEquals(id, loaded.getId());
        assertEquals("profile-renamed", loaded.getUsername());
    }

    @Test
    @Order(3)
    void saveAndLoadGamesPlayed() {
        UserDatabase database = UserDatabase.getInstance();
        User user = database.getUser("profile-renamed");
        assertNotNull(user);

        user.recordGamePlayed();
        user.recordGamePlayed();
        database.saveGamesPlayed(user);

        User loaded = database.getUser("profile-renamed");
        assertNotNull(loaded);
        assertEquals(2, loaded.getGamesPlayed());
        assertTrue(loaded.getId() > 0);
    }

    @Test
    @Order(4)
    void updatePasswordByUsernameWorks() {
        UserDatabase database = UserDatabase.getInstance();
        String hash = HashUtil.hashSHA256("LatestPass1!");
        database.updatePassword("profile-renamed", hash);

        User loaded = database.getUser("profile-renamed");
        assertNotNull(loaded);
        assertEquals(hash, loaded.getPasswordHash());
    }
}
