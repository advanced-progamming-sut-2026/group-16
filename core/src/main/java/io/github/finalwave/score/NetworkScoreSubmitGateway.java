package io.github.finalwave.score;

import com.badlogic.gdx.Gdx;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.finalwave.network.MessageEnvelope;
import io.github.finalwave.network.MessageTypes;
import io.github.finalwave.network.NetworkManager;
import io.github.finalwave.network.score.SubmitScoreFailPayload;
import io.github.finalwave.network.score.SubmitScoreOkPayload;
import io.github.finalwave.network.score.SubmitScoreRequest;
import io.github.finalwave.network.sync.ProgressSyncService;
import io.github.finalwave.network.sync.SyncFailReason;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class NetworkScoreSubmitGateway implements ScoreSubmitGateway {
    public static final String NOT_CONNECTED = "NOT_CONNECTED";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final NetworkManager networkManager;
    private final ProgressSyncService progressSyncService;
    private final Map<String, PendingSubmit> pending = new ConcurrentHashMap<>();
    private volatile Callback authRetryCallback;
    private volatile int authRetryScore;

    public NetworkScoreSubmitGateway(NetworkManager networkManager) {
        this(networkManager, null);
    }

    public NetworkScoreSubmitGateway(NetworkManager networkManager, ProgressSyncService progressSyncService) {
        this.networkManager = networkManager;
        this.progressSyncService = progressSyncService;
        networkManager.registerListener(MessageTypes.SUBMIT_SCORE_OK, this::handleOk);
        networkManager.registerListener(MessageTypes.SUBMIT_SCORE_FAIL, this::handleFail);
        networkManager.registerListener(MessageTypes.RESUME_OK, envelope -> retryAfterSession());
        networkManager.registerListener(MessageTypes.LOGIN_OK, envelope -> retryAfterSession());
    }

    @Override
    public void submit(int score, Callback callback) {
        sendSubmit(score, callback, true);
    }

    private void sendSubmit(int score, Callback callback, boolean allowAuthRetry) {
        if (callback == null) {
            return;
        }
        if (!networkManager.isConnected()) {
            callback.onFailure(NOT_CONNECTED);
            return;
        }
        try {
            SubmitScoreRequest request = new SubmitScoreRequest(Math.max(0, score));
            String requestId = networkManager.sendMessage(MessageTypes.SUBMIT_SCORE, request);
            pending.put(requestId, new PendingSubmit(callback, allowAuthRetry, Math.max(0, score)));
        } catch (RuntimeException exception) {
            callback.onFailure(SyncFailReason.SERVER_ERROR);
        }
    }

    private void handleOk(MessageEnvelope envelope) {
        PendingSubmit pendingSubmit = pending.remove(envelope.getRequestId());
        if (pendingSubmit == null) {
            return;
        }
        authRetryCallback = null;
        authRetryScore = 0;
        try {
            SubmitScoreOkPayload payload = MAPPER.treeToValue(envelope.getPayload(), SubmitScoreOkPayload.class);
            runCallback(pendingSubmit.callback(), () -> pendingSubmit.callback().onSuccess(payload));
        } catch (Exception exception) {
            runCallback(pendingSubmit.callback(), () -> pendingSubmit.callback().onFailure(SyncFailReason.SERVER_ERROR));
        }
    }

    private void handleFail(MessageEnvelope envelope) {
        PendingSubmit pendingSubmit = pending.remove(envelope.getRequestId());
        if (pendingSubmit == null) {
            return;
        }
        try {
            SubmitScoreFailPayload payload = MAPPER.treeToValue(envelope.getPayload(), SubmitScoreFailPayload.class);
            String reason = payload.getReason() == null ? SyncFailReason.SERVER_ERROR : payload.getReason();
            if (pendingSubmit.allowAuthRetry()
                    && SyncFailReason.AUTH_REQUIRED.equals(reason)
                    && authRetryCallback == null
                    && progressSyncService != null) {
                authRetryCallback = pendingSubmit.callback();
                authRetryScore = pendingSubmit.score();
                progressSyncService.refreshSession();
                return;
            }
            authRetryCallback = null;
            authRetryScore = 0;
            runCallback(pendingSubmit.callback(), () -> pendingSubmit.callback().onFailure(reason));
        } catch (Exception exception) {
            authRetryCallback = null;
            authRetryScore = 0;
            runCallback(pendingSubmit.callback(), () -> pendingSubmit.callback().onFailure(SyncFailReason.SERVER_ERROR));
        }
    }

    private void retryAfterSession() {
        Callback retry = authRetryCallback;
        if (retry == null) {
            return;
        }
        int score = authRetryScore;
        authRetryCallback = null;
        authRetryScore = 0;
        sendSubmit(score, retry, false);
    }

    private static void runCallback(Callback callback, Runnable action) {
        if (Gdx.app != null) {
            Gdx.app.postRunnable(action);
        } else {
            action.run();
        }
    }

    private record PendingSubmit(Callback callback, boolean allowAuthRetry, int score) {
    }
}
