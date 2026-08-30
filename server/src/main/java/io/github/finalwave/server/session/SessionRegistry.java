package io.github.finalwave.server.session;

import io.github.finalwave.network.auth.LoginFailReason;
import io.github.finalwave.server.ClientHandler;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class SessionRegistry {
    private final ConcurrentHashMap<String, ClientHandler> usernameToHandler = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<ClientHandler, String> handlerToUsername = new ConcurrentHashMap<>();

    public Optional<String> tryBind(String username, ClientHandler handler) {
        if (username == null || username.isBlank() || handler == null) {
            return Optional.of(LoginFailReason.INVALID_INPUT);
        }
        ClientHandler existing = usernameToHandler.putIfAbsent(username, handler);
        if (existing != null && existing != handler) {
            return Optional.of(LoginFailReason.ALREADY_LOGGED_IN);
        }
        handlerToUsername.put(handler, username);
        return Optional.empty();
    }

    public void unbind(ClientHandler handler) {
        if (handler == null) {
            return;
        }
        String username = handlerToUsername.remove(handler);
        if (username != null) {
            usernameToHandler.remove(username, handler);
        }
    }

    public Optional<String> usernameFor(ClientHandler handler) {
        return Optional.ofNullable(handlerToUsername.get(handler));
    }
}
