package io.github.finalwave.server;

import io.github.finalwave.server.db.ServerDatabase;
import io.github.finalwave.server.matchmaking.MatchRegistry;
import io.github.finalwave.server.matchmaking.RandomQueue;
import io.github.finalwave.server.session.SessionRegistry;

public final class ServerContext {
    private final ServerDatabase database;
    private final SessionRegistry sessionRegistry;
    private final MatchRegistry matchRegistry;
    private final RandomQueue randomQueue;

    public ServerContext(ServerDatabase database, SessionRegistry sessionRegistry) {
        MatchRegistry matchRegistry = new MatchRegistry();
        this.database = database;
        this.sessionRegistry = sessionRegistry;
        this.matchRegistry = matchRegistry;
        this.randomQueue = new RandomQueue(this);
    }

    public ServerContext(
            ServerDatabase database,
            SessionRegistry sessionRegistry,
            MatchRegistry matchRegistry,
            RandomQueue randomQueue) {
        this.database = database;
        this.sessionRegistry = sessionRegistry;
        this.matchRegistry = matchRegistry;
        this.randomQueue = randomQueue;
    }

    public ServerDatabase database() {
        return database;
    }

    public SessionRegistry sessionRegistry() {
        return sessionRegistry;
    }

    public MatchRegistry matchRegistry() {
        return matchRegistry;
    }

    public RandomQueue randomQueue() {
        return randomQueue;
    }
}
