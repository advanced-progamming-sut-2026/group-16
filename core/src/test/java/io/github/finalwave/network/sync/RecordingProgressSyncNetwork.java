package io.github.finalwave.network.sync;

import io.github.finalwave.network.MessageEnvelope;
import io.github.finalwave.network.MessageTypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

final class RecordingProgressSyncNetwork implements ProgressSyncNetwork {
    private final Map<String, List<Consumer<MessageEnvelope>>> listeners = new HashMap<>();
    private final List<SentMessage> sent = new ArrayList<>();
    private boolean connected = true;
    private boolean sendSucceeds = true;

    @Override
    public String trySend(String type, Object payload) {
        if (!sendSucceeds) {
            return null;
        }
        String requestId = UUID.randomUUID().toString();
        sent.add(new SentMessage(type, payload, requestId));
        return requestId;
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public void registerListener(String type, Consumer<MessageEnvelope> listener) {
        listeners.computeIfAbsent(type, ignored -> new ArrayList<>()).add(listener);
    }

    @Override
    public void armReconnect(String host, int port) {
    }

    @Override
    public void disarmReconnect() {
    }

    void setConnected(boolean connected) {
        this.connected = connected;
    }

    void setSendSucceeds(boolean sendSucceeds) {
        this.sendSucceeds = sendSucceeds;
    }

    List<SentMessage> sent() {
        return List.copyOf(sent);
    }

    void dispatchOk(String requestType, String okType, Object payload) {
        SentMessage message = sent.stream()
                .filter(entry -> requestType.equals(entry.type()))
                .findFirst()
                .orElseThrow();
        List<Consumer<MessageEnvelope>> typeListeners = listeners.get(okType);
        if (typeListeners == null) {
            return;
        }
        MessageEnvelope envelope = new MessageEnvelope(
                okType,
                message.requestId(),
                new com.fasterxml.jackson.databind.ObjectMapper().valueToTree(payload)
        );
        for (Consumer<MessageEnvelope> listener : typeListeners) {
            listener.accept(envelope);
        }
    }

    void dispatchFail(String requestType, String failType) {
        SentMessage message = sent.stream()
                .filter(entry -> requestType.equals(entry.type()))
                .findFirst()
                .orElseThrow();
        List<Consumer<MessageEnvelope>> typeListeners = listeners.get(failType);
        if (typeListeners == null) {
            return;
        }
        MessageEnvelope envelope = new MessageEnvelope(
                failType,
                message.requestId(),
                new com.fasterxml.jackson.databind.ObjectMapper().createObjectNode()
        );
        for (Consumer<MessageEnvelope> listener : typeListeners) {
            listener.accept(envelope);
        }
    }

    record SentMessage(String type, Object payload, String requestId) {
    }
}
