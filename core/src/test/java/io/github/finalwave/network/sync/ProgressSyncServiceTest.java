package io.github.finalwave.network.sync;

import io.github.finalwave.model.App;
import io.github.finalwave.model.user.Gender;
import io.github.finalwave.model.user.User;
import io.github.finalwave.model.user.UserDatabase;
import io.github.finalwave.network.MessageTypes;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProgressSyncServiceTest {
    private static final Path DATABASE = Path.of("target", "progress-sync-service-test.db");

    private RecordingProgressSyncNetwork network;
    private UserDatabase database;
    private ProgressSyncService service;
    private User user;

    @BeforeEach
    void setUp() throws Exception {
        Files.createDirectories(DATABASE.getParent());
        Files.deleteIfExists(DATABASE);
        System.setProperty("pvz.database.url", "jdbc:sqlite:" + DATABASE.toAbsolutePath());
        UserDatabase.resetInstanceForTests();
        database = UserDatabase.getInstance();
        network = new RecordingProgressSyncNetwork();
        service = new ProgressSyncService(network, database, "127.0.0.1", 5454);
        database.addWriteListener(service);
        user = registerUser("sync-client");
        App.getInstance().setCurrentUser(user);
        service.arm();
    }

    @AfterEach
    void tearDown() throws Exception {
        App.getInstance().setCurrentUser(null);
        UserDatabase.resetInstanceForTests();
        System.clearProperty("pvz.database.url");
        Files.deleteIfExists(DATABASE);
    }

    @Test
    void walletWriteSendsUpdateWhenConnected() {
        user.addCoins(50);
        database.saveUserWallet(user);

        assertEquals(1, network.sent().size());
        assertEquals(MessageTypes.UPDATE_WALLET, network.sent().get(0).type());
        UpdateWalletPayload payload = new com.fasterxml.jackson.databind.ObjectMapper()
                .convertValue(network.sent().get(0).payload(), UpdateWalletPayload.class);
        assertEquals(50, payload.getCoins());
    }

    @Test
    void walletWriteQueuesWhenDisconnectedAndFlushesOnReconnect() {
        network.setSendSucceeds(false);
        user.addCoins(25);
        database.saveUserWallet(user);
        assertEquals(1, service.pendingQueueSize());
        assertTrue(network.sent().isEmpty());

        network.setSendSucceeds(true);
        service.onConnected();

        assertEquals(0, service.pendingQueueSize());
        assertEquals(1, network.sent().stream()
                .filter(message -> MessageTypes.UPDATE_WALLET.equals(message.type()))
                .count());
    }

    @Test
    void syncFailRequeuesWrite() {
        user.addCoins(10);
        database.saveUserWallet(user);
        assertEquals(1, network.sent().size());

        network.dispatchFail(MessageTypes.UPDATE_WALLET, MessageTypes.UPDATE_WALLET_FAIL);
        assertEquals(2, network.sent().stream()
                .filter(message -> MessageTypes.UPDATE_WALLET.equals(message.type()))
                .count());
    }

    @Test
    void ignoresWritesForDifferentUser() {
        User other = registerUser("other-user");
        other.addCoins(999);
        database.saveUserWallet(other);
        assertTrue(network.sent().isEmpty());
    }

    private User registerUser(String username) {
        User registered = new User(username, "hash", "Nick", username + "@test.com", Gender.MALE);
        database.registerUser(registered);
        return database.getUser(username);
    }
}
