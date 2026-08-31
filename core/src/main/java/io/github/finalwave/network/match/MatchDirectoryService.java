package io.github.finalwave.network.match;

import com.badlogic.gdx.Gdx;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.finalwave.network.MessageEnvelope;
import io.github.finalwave.network.MessageTypes;
import io.github.finalwave.network.NetworkManager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class MatchDirectoryService {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final NetworkManager networkManager;
    private final Map<String, Consumer<ListMatchUsersResponse>> listPending = new ConcurrentHashMap<>();
    private final Map<String, Runnable> resetPending = new ConcurrentHashMap<>();
    private volatile Consumer<ListMatchUsersResponse> updateListener;

    public MatchDirectoryService(NetworkManager networkManager) {
        this.networkManager = networkManager;
        networkManager.registerListener(MessageTypes.LIST_MATCH_USERS_OK, this::handleListOk);
        networkManager.registerListener(MessageTypes.MATCHMAKING_RESET_OK, this::handleResetOk);
        networkManager.registerListener(MessageTypes.MATCH_USERS_UPDATED, this::handleUsersUpdated);
    }

    public void refresh(Consumer<ListMatchUsersResponse> onResult) {
        if (!networkManager.isConnected()) {
            Gdx.app.postRunnable(() -> onResult.accept(emptyResponse()));
            return;
        }
        resetMatchmaking(() -> list(onResult));
    }

    public void list(Consumer<ListMatchUsersResponse> onResult) {
        if (!networkManager.isConnected()) {
            Gdx.app.postRunnable(() -> onResult.accept(emptyResponse()));
            return;
        }
        requestList(onResult);
    }

    public void setUpdateListener(Consumer<ListMatchUsersResponse> listener) {
        this.updateListener = listener;
    }

    private void handleUsersUpdated(MessageEnvelope envelope) {
        Consumer<ListMatchUsersResponse> listener = updateListener;
        if (listener == null) {
            return;
        }
        ListMatchUsersResponse response = emptyResponse();
        try {
            ListMatchUsersResponse parsed = MAPPER.treeToValue(
                    envelope.getPayload(), ListMatchUsersResponse.class);
            if (parsed != null) {
                response = parsed;
            }
        } catch (Exception ignored) {
        }
        ListMatchUsersResponse finalResponse = response;
        Gdx.app.postRunnable(() -> listener.accept(finalResponse));
    }

    public void resetMatchmaking(Runnable onDone) {
        if (!networkManager.isConnected()) {
            if (onDone != null) {
                Gdx.app.postRunnable(onDone);
            }
            return;
        }
        try {
            String requestId = networkManager.sendMessage(MessageTypes.MATCHMAKING_RESET, Map.of());
            if (onDone == null) {
                return;
            }
            resetPending.put(requestId, onDone);
        } catch (RuntimeException exception) {
            if (onDone != null) {
                Gdx.app.postRunnable(onDone);
            }
        }
    }

    private void requestList(Consumer<ListMatchUsersResponse> onResult) {
        try {
            String requestId = networkManager.sendMessage(MessageTypes.LIST_MATCH_USERS, Map.of());
            listPending.put(requestId, onResult);
        } catch (RuntimeException exception) {
            Gdx.app.postRunnable(() -> onResult.accept(emptyResponse()));
        }
    }

    private void handleListOk(MessageEnvelope envelope) {
        Consumer<ListMatchUsersResponse> callback = listPending.remove(envelope.getRequestId());
        if (callback == null) {
            return;
        }
        ListMatchUsersResponse response = emptyResponse();
        try {
            ListMatchUsersResponse parsed = MAPPER.treeToValue(
                    envelope.getPayload(), ListMatchUsersResponse.class);
            if (parsed != null) {
                response = parsed;
            }
        } catch (Exception ignored) {
        }
        ListMatchUsersResponse finalResponse = response;
        Gdx.app.postRunnable(() -> callback.accept(finalResponse));
    }

    private void handleResetOk(MessageEnvelope envelope) {
        Runnable callback = resetPending.remove(envelope.getRequestId());
        if (callback != null) {
            Gdx.app.postRunnable(callback);
        }
    }

    private static ListMatchUsersResponse emptyResponse() {
        return new ListMatchUsersResponse("", false, false, java.util.List.of());
    }
}
