package io.github.finalwave.network.sync;

import io.github.finalwave.network.MessageEnvelope;

import java.util.function.Consumer;

public interface ProgressSyncNetwork {
    String trySend(String type, Object payload);

    boolean isConnected();

    void registerListener(String type, Consumer<MessageEnvelope> listener);

    void armReconnect(String host, int port);

    void disarmReconnect();
}
