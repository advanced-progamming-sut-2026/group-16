package io.github.finalwave.network;

import com.badlogic.gdx.Gdx;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class NetworkPingProbe {
    private final NetworkManager networkManager;
    private final Map<String, Long> pendingPingMillis = new ConcurrentHashMap<>();

    public NetworkPingProbe(NetworkManager networkManager) {
        this.networkManager = networkManager;
    }

    public void start(String host, int port) {
        networkManager.setConnectionListener(new NetworkManager.ConnectionListener() {
            @Override
            public void onConnected() {
                Gdx.app.log("Network", "Connected to " + host + ":" + port);
                sendPing();
            }

            @Override
            public void onDisconnected(String reason) {
                Gdx.app.log("Network", "Connection lost: " + reason);
                pendingPingMillis.clear();
            }
        });
        networkManager.registerListener(MessageTypes.PONG, this::handlePong);
        try {
            networkManager.connect(host, port);
        } catch (RuntimeException exception) {
            Gdx.app.log("Network", "Server unavailable: " + exception.getMessage());
        }
    }

    private void sendPing() {
        if (!networkManager.isConnected()) {
            return;
        }
        String requestId = networkManager.sendMessage(MessageTypes.PING, null);
        pendingPingMillis.put(requestId, System.currentTimeMillis());
    }

    private void handlePong(MessageEnvelope envelope) {
        Long sentAt = pendingPingMillis.remove(envelope.getRequestId());
        if (sentAt == null) {
            return;
        }
        long roundTripMillis = System.currentTimeMillis() - sentAt;
        Gdx.app.log("Network", "PONG received in " + roundTripMillis + " ms");
    }
}
