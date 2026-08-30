package io.github.finalwave.server;

import io.github.finalwave.server.db.ServerDatabase;
import io.github.finalwave.server.session.SessionRegistry;

public final class ServerContext {
    private final ServerDatabase database;
    private final SessionRegistry sessionRegistry;

    public ServerContext(ServerDatabase database, SessionRegistry sessionRegistry) {
        this.database = database;
        this.sessionRegistry = sessionRegistry;
    }

    public ServerDatabase database() {
        return database;
    }

    public SessionRegistry sessionRegistry() {
        return sessionRegistry;
    }
}
