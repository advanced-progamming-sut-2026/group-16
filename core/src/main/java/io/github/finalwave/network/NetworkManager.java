package io.github.finalwave.network;

import com.badlogic.gdx.Gdx;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import io.github.finalwave.network.sync.ProgressSyncNetwork;

public final class NetworkManager implements ProgressSyncNetwork {
    private static final long RECONNECT_BACKOFF_MS = 5000L;

    public interface ConnectionListener {
        void onConnected();

        void onDisconnected(String reason);
    }

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final Map<String, List<Consumer<MessageEnvelope>>> listeners = new ConcurrentHashMap<>();
    private final ConcurrentLinkedQueue<MessageEnvelope> incoming = new ConcurrentLinkedQueue<>();

    private volatile ConnectionListener connectionListener;
    private final CopyOnWriteArrayList<ConnectionListener> connectionListeners = new CopyOnWriteArrayList<>();
    private volatile Socket socket;
    private volatile JsonLineProtocol protocol;
    private volatile Thread readerThread;
    private volatile boolean connected;
    private volatile boolean disconnectNotified;
    private volatile boolean reconnectArmed;
    private volatile String reconnectHost;
    private volatile int reconnectPort;
    private volatile Thread reconnectThread;

    public void setConnectionListener(ConnectionListener connectionListener) {
        this.connectionListener = connectionListener;
    }

    public void addConnectionListener(ConnectionListener connectionListener) {
        if (connectionListener != null) {
            connectionListeners.add(connectionListener);
        }
    }

    public void connect(String host, int port) {
        disconnect("Reconnecting");
        try {
            Socket activeSocket = new Socket();
            activeSocket.connect(new InetSocketAddress(host, port), 5000);
            socket = activeSocket;
            protocol = new JsonLineProtocol(MAPPER, activeSocket.getInputStream(), activeSocket.getOutputStream());
            connected = true;
            disconnectNotified = false;
            readerThread = new Thread(this::readLoop, "network-reader");
            readerThread.setDaemon(true);
            readerThread.start();
            notifyConnected();
        } catch (IOException exception) {
            closeSocketQuietly();
            connected = false;
            protocol = null;
            notifyDisconnected("Connect failed: " + exception.getMessage());
            throw new IllegalStateException("Failed to connect to " + host + ":" + port, exception);
        }
    }

    public void connectAsync(String host, int port) {
        Thread thread = new Thread(() -> {
            try {
                connect(host, port);
            } catch (RuntimeException exception) {
                scheduleReconnect();
            }
        }, "network-connect");
        thread.setDaemon(true);
        thread.start();
    }

    public void armReconnect(String host, int port) {
        reconnectArmed = true;
        reconnectHost = host;
        reconnectPort = port;
    }

    public void disarmReconnect() {
        reconnectArmed = false;
        reconnectHost = null;
        reconnectPort = 0;
        Thread thread = reconnectThread;
        reconnectThread = null;
        if (thread != null) {
            thread.interrupt();
        }
    }

    public boolean isConnected() {
        return connected;
    }

    public void registerListener(String type, Consumer<MessageEnvelope> listener) {
        listeners.computeIfAbsent(type, ignored -> new ArrayList<>()).add(listener);
    }

    public String sendMessage(String type, Object payload) {
        JsonLineProtocol activeProtocol = protocol;
        if (!connected || activeProtocol == null) {
            throw new IllegalStateException("Not connected");
        }
        String requestId = UUID.randomUUID().toString();
        JsonNode payloadNode = payload == null ? NullNode.getInstance() : MAPPER.valueToTree(payload);
        MessageEnvelope envelope = new MessageEnvelope(type, requestId, payloadNode);
        try {
            activeProtocol.send(envelope);
        } catch (IOException exception) {
            handleReadFailure("Send failed: " + exception.getMessage());
            throw new IllegalStateException("Failed to send " + type, exception);
        }
        return requestId;
    }

    public String trySend(String type, Object payload) {
        try {
            return sendMessage(type, payload);
        } catch (RuntimeException exception) {
            return null;
        }
    }

    public void drainIncoming() {
        MessageEnvelope message;
        while ((message = incoming.poll()) != null) {
            MessageEnvelope current = message;
            Gdx.app.postRunnable(() -> dispatch(current));
        }
    }

    public void disconnect() {
        disconnect("Disconnected");
    }

    private void readLoop() {
        try {
            JsonLineProtocol activeProtocol = protocol;
            while (connected && activeProtocol != null) {
                MessageEnvelope message = activeProtocol.receive();
                if (message == null) {
                    handleReadFailure("Connection closed");
                    return;
                }
                incoming.offer(message);
            }
        } catch (IOException exception) {
            handleReadFailure(exception.getMessage());
        }
    }

    private void dispatch(MessageEnvelope message) {
        List<Consumer<MessageEnvelope>> typeListeners = listeners.get(message.getType());
        if (typeListeners == null) {
            return;
        }
        for (Consumer<MessageEnvelope> listener : typeListeners) {
            listener.accept(message);
        }
    }

    private void handleReadFailure(String reason) {
        connected = false;
        protocol = null;
        closeSocketQuietly();
        Thread currentReader = readerThread;
        if (currentReader != null && currentReader != Thread.currentThread()) {
            currentReader.interrupt();
        }
        notifyDisconnected(reason);
        scheduleReconnect();
    }

    private void disconnect(String reason) {
        connected = false;
        protocol = null;
        closeSocketQuietly();
        Thread currentReader = readerThread;
        readerThread = null;
        if (currentReader != null) {
            currentReader.interrupt();
        }
        notifyDisconnected(reason);
    }

    private void scheduleReconnect() {
        if (!reconnectArmed || reconnectHost == null) {
            return;
        }
        if (reconnectThread != null && reconnectThread.isAlive()) {
            return;
        }
        reconnectThread = new Thread(() -> {
            while (reconnectArmed && !connected) {
                try {
                    Thread.sleep(RECONNECT_BACKOFF_MS);
                } catch (InterruptedException exception) {
                    return;
                }
                if (!reconnectArmed || connected) {
                    return;
                }
                try {
                    connect(reconnectHost, reconnectPort);
                    return;
                } catch (RuntimeException ignored) {
                }
            }
        }, "network-reconnect");
        reconnectThread.setDaemon(true);
        reconnectThread.start();
    }

    private void closeSocketQuietly() {
        Socket activeSocket = socket;
        socket = null;
        if (activeSocket == null) {
            return;
        }
        try {
            activeSocket.close();
        } catch (IOException ignored) {
        }
    }

    private void notifyConnected() {
        ConnectionListener listener = connectionListener;
        if (listener != null) {
            Gdx.app.postRunnable(listener::onConnected);
        }
        for (ConnectionListener extra : connectionListeners) {
            Gdx.app.postRunnable(extra::onConnected);
        }
    }

    private void notifyDisconnected(String reason) {
        if (disconnectNotified) {
            return;
        }
        disconnectNotified = true;
        ConnectionListener listener = connectionListener;
        if (listener != null) {
            Gdx.app.postRunnable(() -> listener.onDisconnected(reason));
        }
        for (ConnectionListener extra : connectionListeners) {
            Gdx.app.postRunnable(() -> extra.onDisconnected(reason));
        }
    }
}
