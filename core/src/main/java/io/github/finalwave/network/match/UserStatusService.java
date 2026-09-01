package io.github.finalwave.network.match;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.finalwave.network.MessageEnvelope;
import io.github.finalwave.network.MessageTypes;
import io.github.finalwave.network.NetworkManager;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public final class UserStatusService {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final long AWAIT_MS = 5000L;

    private final NetworkManager networkManager;
    private final Map<String, CompletableFuture<UserStatus>> pending = new ConcurrentHashMap<>();

    public UserStatusService(NetworkManager networkManager) {
        this.networkManager = networkManager;
        networkManager.registerListener(MessageTypes.CHECK_USER_STATUS_OK, this::handleOk);
    }

    public Optional<UserStatus> check(String username) {
        if (!networkManager.isConnected()) {
            return Optional.empty();
        }
        try {
            String requestId = networkManager.sendMessage(
                    MessageTypes.CHECK_USER_STATUS, new CheckUserStatusRequest(username));
            CompletableFuture<UserStatus> future = new CompletableFuture<>();
            pending.put(requestId, future);
            UserStatus status = future.get(AWAIT_MS, TimeUnit.MILLISECONDS);
            return Optional.ofNullable(status);
        } catch (Exception exception) {
            return Optional.empty();
        }
    }

    private void handleOk(MessageEnvelope envelope) {
        CompletableFuture<UserStatus> future = pending.remove(envelope.getRequestId());
        if (future == null) {
            return;
        }
        try {
            CheckUserStatusResponse response = MAPPER.treeToValue(
                    envelope.getPayload(), CheckUserStatusResponse.class);
            future.complete(response == null ? UserStatus.NOT_FOUND : response.getStatus());
        } catch (Exception exception) {
            future.complete(UserStatus.NOT_FOUND);
        }
    }
}
