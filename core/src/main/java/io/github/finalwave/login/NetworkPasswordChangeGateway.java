package io.github.finalwave.login;

import com.badlogic.gdx.Gdx;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.finalwave.network.MessageEnvelope;
import io.github.finalwave.network.MessageTypes;
import io.github.finalwave.network.NetworkManager;
import io.github.finalwave.network.auth.ChangePasswordRequest;
import io.github.finalwave.network.auth.PasswordChangeFailPayload;
import io.github.finalwave.network.auth.PasswordChangeFailReason;
import io.github.finalwave.network.auth.PasswordChangeOkPayload;
import io.github.finalwave.network.auth.ResetPasswordRequest;
import io.github.finalwave.network.auth.SecurityQuestionLookupOkPayload;
import io.github.finalwave.network.auth.SecurityQuestionLookupRequest;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class NetworkPasswordChangeGateway implements PasswordChangeGateway {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static volatile NetworkPasswordChangeGateway instance;

    private final NetworkManager networkManager;
    private final Map<String, Callback> pending = new ConcurrentHashMap<>();
    private final Map<String, LookupCallback> pendingLookups = new ConcurrentHashMap<>();

    public NetworkPasswordChangeGateway(NetworkManager networkManager) {
        this.networkManager = networkManager;
        networkManager.registerListener(MessageTypes.RESET_PASSWORD_OK, this::handleResetOk);
        networkManager.registerListener(MessageTypes.RESET_PASSWORD_FAIL, this::handleResetFail);
        networkManager.registerListener(MessageTypes.CHANGE_PASSWORD_OK, this::handleChangeOk);
        networkManager.registerListener(MessageTypes.CHANGE_PASSWORD_FAIL, this::handleChangeFail);
        networkManager.registerListener(MessageTypes.SECURITY_QUESTION_LOOKUP_OK, this::handleLookupOk);
        networkManager.registerListener(MessageTypes.SECURITY_QUESTION_LOOKUP_FAIL, this::handleLookupFail);
        instance = this;
    }

    public static PasswordChangeGateway getInstance() {
        return instance;
    }

    @Override
    public void resetPassword(ResetPasswordRequest request, Callback callback) {
        send(MessageTypes.RESET_PASSWORD, request, callback);
    }

    @Override
    public void changePassword(ChangePasswordRequest request, Callback callback) {
        send(MessageTypes.CHANGE_PASSWORD, request, callback);
    }

    @Override
    public void lookupSecurityQuestion(SecurityQuestionLookupRequest request, LookupCallback callback) {
        if (!networkManager.isConnected()) {
            callback.onFailure(new PasswordChangeFailPayload(PasswordChangeFailReason.NOT_CONNECTED));
            return;
        }
        try {
            String requestId = networkManager.sendMessage(MessageTypes.SECURITY_QUESTION_LOOKUP, request);
            pendingLookups.put(requestId, callback);
        } catch (RuntimeException exception) {
            callback.onFailure(new PasswordChangeFailPayload(PasswordChangeFailReason.SERVER_ERROR));
        }
    }

    private void send(String type, Object request, Callback callback) {
        if (!networkManager.isConnected()) {
            callback.onFailure(new PasswordChangeFailPayload(PasswordChangeFailReason.NOT_CONNECTED));
            return;
        }
        try {
            String requestId = networkManager.sendMessage(type, request);
            pending.put(requestId, callback);
        } catch (RuntimeException exception) {
            callback.onFailure(new PasswordChangeFailPayload(PasswordChangeFailReason.SERVER_ERROR));
        }
    }

    private void handleResetOk(MessageEnvelope envelope) {
        deliverOk(envelope);
    }

    private void handleResetFail(MessageEnvelope envelope) {
        deliverFail(envelope);
    }

    private void handleChangeOk(MessageEnvelope envelope) {
        deliverOk(envelope);
    }

    private void handleChangeFail(MessageEnvelope envelope) {
        deliverFail(envelope);
    }

    private void handleLookupOk(MessageEnvelope envelope) {
        LookupCallback callback = pendingLookups.remove(envelope.getRequestId());
        if (callback == null) {
            return;
        }
        try {
            SecurityQuestionLookupOkPayload payload =
                    MAPPER.treeToValue(envelope.getPayload(), SecurityQuestionLookupOkPayload.class);
            Gdx.app.postRunnable(() -> callback.onSuccess(payload));
        } catch (Exception exception) {
            Gdx.app.postRunnable(() ->
                    callback.onFailure(new PasswordChangeFailPayload(PasswordChangeFailReason.SERVER_ERROR)));
        }
    }

    private void handleLookupFail(MessageEnvelope envelope) {
        LookupCallback callback = pendingLookups.remove(envelope.getRequestId());
        if (callback == null) {
            return;
        }
        try {
            PasswordChangeFailPayload payload =
                    MAPPER.treeToValue(envelope.getPayload(), PasswordChangeFailPayload.class);
            Gdx.app.postRunnable(() -> callback.onFailure(payload));
        } catch (Exception exception) {
            Gdx.app.postRunnable(() ->
                    callback.onFailure(new PasswordChangeFailPayload(PasswordChangeFailReason.SERVER_ERROR)));
        }
    }

    private void deliverOk(MessageEnvelope envelope) {
        Callback callback = pending.remove(envelope.getRequestId());
        if (callback == null) {
            return;
        }
        try {
            PasswordChangeOkPayload payload = MAPPER.treeToValue(envelope.getPayload(), PasswordChangeOkPayload.class);
            Gdx.app.postRunnable(() -> callback.onSuccess(payload));
        } catch (Exception exception) {
            Gdx.app.postRunnable(() ->
                    callback.onFailure(new PasswordChangeFailPayload(PasswordChangeFailReason.SERVER_ERROR)));
        }
    }

    private void deliverFail(MessageEnvelope envelope) {
        Callback callback = pending.remove(envelope.getRequestId());
        if (callback == null) {
            return;
        }
        try {
            PasswordChangeFailPayload payload =
                    MAPPER.treeToValue(envelope.getPayload(), PasswordChangeFailPayload.class);
            Gdx.app.postRunnable(() -> callback.onFailure(payload));
        } catch (Exception exception) {
            Gdx.app.postRunnable(() ->
                    callback.onFailure(new PasswordChangeFailPayload(PasswordChangeFailReason.SERVER_ERROR)));
        }
    }
}
