package io.github.finalwave.server.presence;

import io.github.finalwave.model.user.Gender;
import io.github.finalwave.model.user.User;
import io.github.finalwave.network.match.UserStatus;
import io.github.finalwave.server.ClientHandler;
import io.github.finalwave.server.ServerContext;
import io.github.finalwave.server.db.ServerDatabase;
import io.github.finalwave.server.session.SessionRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserStatusHandlerTest {

    private ServerDatabase database;
    private SessionRegistry registry;
    private ServerContext context;

    @BeforeEach
    void setUp() {
        database = new ServerDatabase();
        database.initializeSchema();
        database.registerUser(new User("bob", "hash", "bob", "bob@test.com", Gender.MALE));
        registry = new SessionRegistry();
        context = new ServerContext(database, registry);
    }

    @Test
    void resolvesOnlineOfflineAndNotFound() throws Exception {
        ClientHandler onlineHandler = new ClientHandler(null, context);
        ClientHandler checker = new ClientHandler(null, context);
        registry.tryBind("alice", onlineHandler);
        database.registerUser(new User("alice", "hash", "alice", "alice@test.com", Gender.FEMALE));

        UserStatusHandler handler = new UserStatusHandler(context, checker);
        assertEquals(UserStatus.ONLINE, UserStatusHandlerTestSupport.parseStatus(
                handler.handle(UserStatusHandlerTestSupport.request("alice"))));
        assertEquals(UserStatus.OFFLINE, UserStatusHandlerTestSupport.parseStatus(
                handler.handle(UserStatusHandlerTestSupport.request("bob"))));
        assertEquals(UserStatus.NOT_FOUND, UserStatusHandlerTestSupport.parseStatus(
                handler.handle(UserStatusHandlerTestSupport.request("missing"))));
    }
}
