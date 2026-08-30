package io.github.finalwave.registration;

import com.badlogic.gdx.Gdx;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.finalwave.network.MessageEnvelope;
import io.github.finalwave.network.MessageTypes;
import io.github.finalwave.network.NetworkManager;
import io.github.finalwave.network.auth.RegisterFailPayload;
import io.github.finalwave.network.auth.RegisterFailReason;
import io.github.finalwave.network.auth.RegisterOkPayload;
import io.github.finalwave.network.auth.RegisterRequest;
import io.github.finalwave.util.RegisterRequestValidator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class NetworkRegistrationGateway implements RegistrationGateway {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final NetworkManager networkManager;
    private final Map<String, Callback> pending = new ConcurrentHashMap<>();

    public NetworkRegistrationGateway(NetworkManager networkManager) {
        this.networkManager = networkManager;
        networkManager.registerListener(MessageTypes.REGISTER_OK, this::handleOk);
        networkManager.registerListener(MessageTypes.REGISTER_FAIL, this::handleFail);
    }

    @Override
    public void register(RegisterRequest request, Callback callback) {
        String validationFailure = RegisterRequestValidator.validate(request);
        if (validationFailure != null) {
            callback.onFailure(new RegisterFailPayload(validationFailure));
            return;
        }
        if (!networkManager.isConnected()) {
            callback.onFailure(new RegisterFailPayload(RegisterFailReason.NOT_CONNECTED));
            return;
        }
        try {
            String requestId = networkManager.sendMessage(MessageTypes.REGISTER, request);
            pending.put(requestId, callback);
        } catch (RuntimeException exception) {
            callback.onFailure(new RegisterFailPayload(RegisterFailReason.SERVER_ERROR));
        }
    }

    private void handleOk(MessageEnvelope envelope) {
        Callback callback = pending.remove(envelope.getRequestId());
        if (callback == null) {
            return;
        }
        try {
            RegisterOkPayload payload = MAPPER.treeToValue(envelope.getPayload(), RegisterOkPayload.class);
            Gdx.app.postRunnable(() -> callback.onSuccess(payload));
        } catch (Exception exception) {
            Gdx.app.postRunnable(() -> callback.onFailure(new RegisterFailPayload(RegisterFailReason.SERVER_ERROR)));
        }
    }

    private void handleFail(MessageEnvelope envelope) {
        Callback callback = pending.remove(envelope.getRequestId());
        if (callback == null) {
            return;
        }
        try {
            RegisterFailPayload payload = MAPPER.treeToValue(envelope.getPayload(), RegisterFailPayload.class);
            Gdx.app.postRunnable(() -> callback.onFailure(payload));
        } catch (Exception exception) {
            Gdx.app.postRunnable(() -> callback.onFailure(new RegisterFailPayload(RegisterFailReason.SERVER_ERROR)));
        }
    }
}
