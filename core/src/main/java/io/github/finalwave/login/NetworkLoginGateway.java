package io.github.finalwave.login;

import com.badlogic.gdx.Gdx;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.finalwave.network.MessageEnvelope;
import io.github.finalwave.network.MessageTypes;
import io.github.finalwave.network.NetworkManager;
import io.github.finalwave.network.auth.LoginFailPayload;
import io.github.finalwave.network.auth.LoginFailReason;
import io.github.finalwave.network.auth.LoginOkPayload;
import io.github.finalwave.network.auth.LoginRequest;
import io.github.finalwave.network.sync.ProgressSyncService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class NetworkLoginGateway implements LoginGateway {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final NetworkManager networkManager;
    private final ProgressSyncService progressSyncService;
    private final Map<String, Callback> pending = new ConcurrentHashMap<>();

    public NetworkLoginGateway(NetworkManager networkManager) {
        this(networkManager, null);
    }

    public NetworkLoginGateway(NetworkManager networkManager, ProgressSyncService progressSyncService) {
        this.networkManager = networkManager;
        this.progressSyncService = progressSyncService;
        networkManager.registerListener(MessageTypes.LOGIN_OK, this::handleOk);
        networkManager.registerListener(MessageTypes.LOGIN_FAIL, this::handleFail);
    }

    @Override
    public void login(LoginRequest request, Callback callback) {
        if (request == null
                || request.getUsername() == null
                || request.getUsername().isBlank()
                || request.getPassword() == null
                || request.getPassword().isBlank()) {
            callback.onFailure(new LoginFailPayload(LoginFailReason.INVALID_INPUT));
            return;
        }
        if (!networkManager.isConnected()) {
            callback.onFailure(new LoginFailPayload(LoginFailReason.NOT_CONNECTED));
            return;
        }
        try {
            String requestId = networkManager.sendMessage(MessageTypes.LOGIN, request);
            pending.put(requestId, callback);
        } catch (RuntimeException exception) {
            callback.onFailure(new LoginFailPayload(LoginFailReason.SERVER_ERROR));
        }
    }

    @Override
    public void logout() {
        if (progressSyncService != null) {
            progressSyncService.disarm();
        }
        if (!networkManager.isConnected()) {
            return;
        }
        try {
            networkManager.sendMessage(MessageTypes.LOGOUT, null);
        } catch (RuntimeException ignored) {
        }
    }

    private void handleOk(MessageEnvelope envelope) {
        if (progressSyncService != null) {
            Gdx.app.postRunnable(progressSyncService::arm);
        }
        Callback callback = pending.remove(envelope.getRequestId());
        if (callback == null) {
            return;
        }
        try {
            LoginOkPayload payload = MAPPER.treeToValue(envelope.getPayload(), LoginOkPayload.class);
            Gdx.app.postRunnable(() -> callback.onSuccess(payload));
        } catch (Exception exception) {
            Gdx.app.postRunnable(() -> callback.onFailure(new LoginFailPayload(LoginFailReason.SERVER_ERROR)));
        }
    }

    private void handleFail(MessageEnvelope envelope) {
        Callback callback = pending.remove(envelope.getRequestId());
        if (callback == null) {
            return;
        }
        try {
            LoginFailPayload payload = MAPPER.treeToValue(envelope.getPayload(), LoginFailPayload.class);
            Gdx.app.postRunnable(() -> callback.onFailure(payload));
        } catch (Exception exception) {
            Gdx.app.postRunnable(() -> callback.onFailure(new LoginFailPayload(LoginFailReason.SERVER_ERROR)));
        }
    }
}
