package io.github.finalwave.network.match;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.finalwave.network.MessageEnvelope;
import io.github.finalwave.network.MessageTypes;
import io.github.finalwave.network.NetworkManager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class MatchmakingService {

    public interface Listener {
        void onInvite(ChallengeInvitePayload invite);

        void onChallengeFail(ChallengeFailReason reason);

        void onChallengeRejected(String inviteId);

        void onChallengeTimeout(String inviteId);

        void onMatchStart(MatchStartPayload start);

        void onQueueWaitingChanged(boolean waiting);
    }

    @FunctionalInterface
    public interface MatchStartHandler {
        void onMatchStart(MatchStartPayload start);
    }

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final NetworkManager networkManager;
    private volatile Listener listener;
    private volatile MatchStartHandler matchStartHandler;
    private volatile boolean queueWaiting;

    public MatchmakingService(NetworkManager networkManager) {
        this.networkManager = networkManager;
        networkManager.registerListener(MessageTypes.CHALLENGE_FAIL, this::handleChallengeFail);
        networkManager.registerListener(MessageTypes.CHALLENGE_INVITE, this::handleInvite);
        networkManager.registerListener(MessageTypes.CHALLENGE_REJECTED, this::handleRejected);
        networkManager.registerListener(MessageTypes.CHALLENGE_TIMEOUT, this::handleTimeout);
        networkManager.registerListener(MessageTypes.MATCH_START, this::handleMatchStart);
        networkManager.registerListener(MessageTypes.JOIN_RANDOM_QUEUE_OK, this::handleQueueOk);
        networkManager.registerListener(MessageTypes.JOIN_RANDOM_QUEUE_FAIL, this::handleQueueFail);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void setMatchStartHandler(MatchStartHandler handler) {
        this.matchStartHandler = handler;
    }

    public boolean isQueueWaiting() {
        return queueWaiting;
    }

    public void challengeUser(String targetUsername) {
        networkManager.trySend(MessageTypes.CHALLENGE_REQUEST, new ChallengeRequest(targetUsername));
    }

    public void respondToInvite(String inviteId, boolean accepted) {
        networkManager.trySend(
                MessageTypes.CHALLENGE_RESPONSE, new ChallengeResponsePayload(inviteId, accepted));
    }

    public void joinRandomQueue() {
        networkManager.trySend(MessageTypes.JOIN_RANDOM_QUEUE, Map.of());
    }

    public void leaveQueue() {
        queueWaiting = false;
        networkManager.trySend(MessageTypes.LEAVE_QUEUE, Map.of());
    }

    private void handleChallengeFail(MessageEnvelope envelope) {
        Listener active = listener;
        if (active == null) {
            return;
        }
        try {
            ChallengeFailPayload payload = MAPPER.treeToValue(envelope.getPayload(), ChallengeFailPayload.class);
            if (payload != null && payload.getReason() != null) {
                active.onChallengeFail(payload.getReason());
            }
        } catch (Exception ignored) {
        }
    }

    private void handleInvite(MessageEnvelope envelope) {
        Listener active = listener;
        if (active == null) {
            return;
        }
        try {
            ChallengeInvitePayload payload = MAPPER.treeToValue(envelope.getPayload(), ChallengeInvitePayload.class);
            if (payload != null) {
                active.onInvite(payload);
            }
        } catch (Exception ignored) {
        }
    }

    private void handleRejected(MessageEnvelope envelope) {
        Listener active = listener;
        if (active == null) {
            return;
        }
        try {
            ChallengeRejectedPayload payload = MAPPER.treeToValue(envelope.getPayload(), ChallengeRejectedPayload.class);
            if (payload != null) {
                active.onChallengeRejected(payload.getInviteId());
            }
        } catch (Exception ignored) {
        }
    }

    private void handleTimeout(MessageEnvelope envelope) {
        Listener active = listener;
        if (active == null) {
            return;
        }
        try {
            ChallengeTimeoutPayload payload = MAPPER.treeToValue(envelope.getPayload(), ChallengeTimeoutPayload.class);
            if (payload != null) {
                active.onChallengeTimeout(payload.getInviteId());
            }
        } catch (Exception ignored) {
        }
    }

    private void handleMatchStart(MessageEnvelope envelope) {
        queueWaiting = false;
        MatchStartPayload payload;
        try {
            payload = MAPPER.treeToValue(envelope.getPayload(), MatchStartPayload.class);
        } catch (Exception ignored) {
            return;
        }
        if (payload == null) {
            return;
        }
        MatchStartHandler launch = matchStartHandler;
        if (launch != null) {
            launch.onMatchStart(payload);
        }
        Listener active = listener;
        if (active != null) {
            active.onMatchStart(payload);
        }
    }

    private void handleQueueOk(MessageEnvelope envelope) {
        try {
            JoinRandomQueueOkPayload payload = MAPPER.treeToValue(envelope.getPayload(), JoinRandomQueueOkPayload.class);
            queueWaiting = payload != null && payload.isWaiting();
            Listener active = listener;
            if (active != null) {
                active.onQueueWaitingChanged(queueWaiting);
            }
        } catch (Exception ignored) {
        }
    }

    private void handleQueueFail(MessageEnvelope envelope) {
        queueWaiting = false;
        Listener active = listener;
        if (active == null) {
            return;
        }
        try {
            JoinRandomQueueFailPayload payload = MAPPER.treeToValue(
                    envelope.getPayload(), JoinRandomQueueFailPayload.class);
            if (payload != null && payload.getReason() != null) {
                active.onChallengeFail(payload.getReason());
            }
        } catch (Exception ignored) {
        }
        active.onQueueWaitingChanged(false);
    }
}
