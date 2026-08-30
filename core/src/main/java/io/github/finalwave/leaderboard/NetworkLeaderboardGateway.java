package io.github.finalwave.leaderboard;

import com.badlogic.gdx.Gdx;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.finalwave.model.leaderboard.LeaderboardEntry;
import io.github.finalwave.network.MessageEnvelope;
import io.github.finalwave.network.MessageTypes;
import io.github.finalwave.network.NetworkManager;
import io.github.finalwave.network.leaderboard.GetLeaderboardFailPayload;
import io.github.finalwave.network.leaderboard.GetLeaderboardOkPayload;
import io.github.finalwave.network.leaderboard.LeaderboardRow;
import io.github.finalwave.network.sync.ProgressSyncService;
import io.github.finalwave.network.sync.SyncFailReason;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class NetworkLeaderboardGateway implements LeaderboardGateway {
    public static final String NOT_CONNECTED = "NOT_CONNECTED";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final NetworkManager networkManager;
    private final ProgressSyncService progressSyncService;
    private final Map<String, PendingFetch> pending = new ConcurrentHashMap<>();
    private volatile Callback authRetryCallback;

    public NetworkLeaderboardGateway(NetworkManager networkManager) {
        this(networkManager, null);
    }

    public NetworkLeaderboardGateway(NetworkManager networkManager, ProgressSyncService progressSyncService) {
        this.networkManager = networkManager;
        this.progressSyncService = progressSyncService;
        networkManager.registerListener(MessageTypes.GET_LEADERBOARD_OK, this::handleOk);
        networkManager.registerListener(MessageTypes.GET_LEADERBOARD_FAIL, this::handleFail);
        networkManager.registerListener(MessageTypes.RESUME_OK, envelope -> retryAfterSession());
        networkManager.registerListener(MessageTypes.LOGIN_OK, envelope -> retryAfterSession());
    }

    @Override
    public void fetch(Callback callback) {
        sendFetch(callback, true);
    }

    private void sendFetch(Callback callback, boolean allowAuthRetry) {
        if (callback == null) {
            return;
        }
        if (!networkManager.isConnected()) {
            callback.onFailure(NOT_CONNECTED);
            return;
        }
        try {
            String requestId = networkManager.sendMessage(MessageTypes.GET_LEADERBOARD, null);
            pending.put(requestId, new PendingFetch(callback, allowAuthRetry));
        } catch (RuntimeException exception) {
            callback.onFailure(SyncFailReason.SERVER_ERROR);
        }
    }

    private void handleOk(MessageEnvelope envelope) {
        PendingFetch pendingFetch = pending.remove(envelope.getRequestId());
        if (pendingFetch == null) {
            return;
        }
        authRetryCallback = null;
        try {
            GetLeaderboardOkPayload payload = MAPPER.treeToValue(envelope.getPayload(), GetLeaderboardOkPayload.class);
            List<LeaderboardEntry> entries = toEntries(payload);
            runCallback(pendingFetch.callback(), () -> pendingFetch.callback().onSuccess(entries));
        } catch (Exception exception) {
            runCallback(pendingFetch.callback(), () -> pendingFetch.callback().onFailure(SyncFailReason.SERVER_ERROR));
        }
    }

    private void handleFail(MessageEnvelope envelope) {
        PendingFetch pendingFetch = pending.remove(envelope.getRequestId());
        if (pendingFetch == null) {
            return;
        }
        try {
            GetLeaderboardFailPayload payload = MAPPER.treeToValue(envelope.getPayload(), GetLeaderboardFailPayload.class);
            String reason = payload.getReason() == null ? SyncFailReason.SERVER_ERROR : payload.getReason();
            if (pendingFetch.allowAuthRetry()
                    && SyncFailReason.AUTH_REQUIRED.equals(reason)
                    && authRetryCallback == null
                    && progressSyncService != null) {
                authRetryCallback = pendingFetch.callback();
                progressSyncService.refreshSession();
                return;
            }
            authRetryCallback = null;
            runCallback(pendingFetch.callback(), () -> pendingFetch.callback().onFailure(reason));
        } catch (Exception exception) {
            authRetryCallback = null;
            runCallback(pendingFetch.callback(), () -> pendingFetch.callback().onFailure(SyncFailReason.SERVER_ERROR));
        }
    }

    private void retryAfterSession() {
        Callback retry = authRetryCallback;
        if (retry == null) {
            return;
        }
        authRetryCallback = null;
        sendFetch(retry, false);
    }

    private static List<LeaderboardEntry> toEntries(GetLeaderboardOkPayload payload) {
        List<LeaderboardEntry> entries = new ArrayList<>();
        if (payload == null || payload.getEntries() == null) {
            return entries;
        }
        for (LeaderboardRow row : payload.getEntries()) {
            if (row == null) {
                continue;
            }
            entries.add(new LeaderboardEntry(
                    row.getUsername(),
                    row.getProgressLabel(),
                    row.getProgressSortKey(),
                    row.getMinigameCount(),
                    row.getDailyQuestCount(),
                    row.getNonDailyQuestCount(),
                    row.getMyPoint()));
        }
        return entries;
    }

    private static void runCallback(Callback callback, Runnable action) {
        if (Gdx.app != null) {
            Gdx.app.postRunnable(action);
        } else {
            action.run();
        }
    }

    private record PendingFetch(Callback callback, boolean allowAuthRetry) {
    }
}
