package io.github.finalwave.server.db;

import io.github.finalwave.model.user.UserDatabase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class DatabaseSanityCheckTest {

    private static final Path DATABASE = Path.of("build", "test-sanity.db");

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
    void roundTripsUserWalletAndScoreGame() {
        ServerDatabase database = new ServerDatabase();
        database.initializeSchema();
        assertDoesNotThrow(() -> DatabaseSanityCheck.runOrThrow(database));
    }

    @Test
    void succeedsWhenRunTwiceAgainstSameDatabase() {
        ServerDatabase database = new ServerDatabase();
        database.initializeSchema();
        DatabaseSanityCheck.runOrThrow(database);
        assertDoesNotThrow(() -> DatabaseSanityCheck.runOrThrow(database));
    }
}
