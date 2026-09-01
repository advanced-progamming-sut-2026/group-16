package io.github.finalwave.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClientDataPathsTest {

    @AfterEach
    void tearDown() {
        System.clearProperty("pvz.database.url");
        System.clearProperty("pvz.session.file");
    }

    @Test
    void sessionFileDerivesFromSqliteDatabaseUrl() {
        System.setProperty("pvz.database.url", "jdbc:sqlite:/tmp/pvz-client-a.db");
        assertEquals(Path.of("/tmp/pvz-client-a.session"), ClientDataPaths.sessionFile());
    }

    @Test
    void sessionFileAppendsSuffixWhenDatabasePathHasNoDbExtension() {
        System.setProperty("pvz.database.url", "jdbc:sqlite:/tmp/pvz-data");
        assertEquals(Path.of("/tmp/pvz-data.session"), ClientDataPaths.sessionFile());
    }

    @Test
    void explicitSessionFilePropertyOverridesDatabaseDerivation() {
        System.setProperty("pvz.database.url", "jdbc:sqlite:/tmp/pvz-client-a.db");
        System.setProperty("pvz.session.file", "/tmp/custom.session");
        assertEquals(Path.of("/tmp/custom.session"), ClientDataPaths.sessionFile());
    }
}
