package io.github.finalwave.network.match;

import com.badlogic.gdx.Gdx;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.MatchResult;
import io.github.finalwave.model.game.board.PlantPlacementResult;
import io.github.finalwave.network.MessageEnvelope;
import io.github.finalwave.network.MessageTypes;
import io.github.finalwave.network.NetworkManager;

public final class MatchSyncService {

    public interface Listener {
        void onMatchEnd(MatchEndPayload payload);
    }

    public interface StateListener {
        void onStateApplied();
    }

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final long SNAPSHOT_INTERVAL_MS = 100L;

    private final NetworkManager networkManager;
    private volatile Listener listener;
    private volatile StateListener stateListener;
    private volatile String matchId;
    private volatile MatchRole role;
    private volatile GameSession hostSession;
    private volatile GameSession guestSession;
    private volatile long lastSnapshotMillis;

    public MatchSyncService(NetworkManager networkManager) {
        this.networkManager = networkManager;
        networkManager.registerListener(MessageTypes.MATCH_INPUT, this::handleInput);
        networkManager.registerListener(MessageTypes.MATCH_STATE, this::handleState);
        networkManager.registerListener(MessageTypes.MATCH_END, this::handleEnd);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void setStateListener(StateListener stateListener) {
        this.stateListener = stateListener;
    }

    public void registerMatch(MatchStartPayload start, GameSession session) {
        if (start == null) {
            return;
        }
        matchId = start.getMatchId();
        role = start.getYourRole();
        if (role == MatchRole.PLANT) {
            hostSession = session;
            guestSession = null;
        } else {
            guestSession = session;
            hostSession = null;
        }
        lastSnapshotMillis = 0L;
        if (role == MatchRole.PLANT) {
            pushHostSnapshotNow();
        }
    }

    public void clear() {
        matchId = null;
        role = null;
        hostSession = null;
        guestSession = null;
    }

    public MatchRole role() {
        return role;
    }

    public String matchId() {
        return matchId;
    }

    public void tickHost() {
        sendHostSnapshot(false);
    }

    public void pushHostSnapshotNow() {
        sendHostSnapshot(true);
    }

    private void sendHostSnapshot(boolean force) {
        GameSession session = hostSession;
        String activeMatchId = matchId;
        if (session == null || activeMatchId == null || role != MatchRole.PLANT) {
            return;
        }
        long now = System.currentTimeMillis();
        if (!force && now - lastSnapshotMillis < SNAPSHOT_INTERVAL_MS) {
            return;
        }
        lastSnapshotMillis = now;
        MatchStatePayload payload = MatchSnapshotBuilder.build(session, activeMatchId);
        networkManager.trySend(MessageTypes.MATCH_STATE, payload);
        if (session.getMatchResult() != MatchResult.IN_PROGRESS) {
            sendHostMatchEnd(session, activeMatchId);
        }
    }

    public void sendGuestPlaceZombie(String alias, int col, int row) {
        String activeMatchId = matchId;
        if (activeMatchId == null || role != MatchRole.ZOMBIE) {
            return;
        }
        MatchInputPayload payload = new MatchInputPayload(
                activeMatchId, MatchInputAction.PLACE_ZOMBIE, alias, col, row);
        networkManager.trySend(MessageTypes.MATCH_INPUT, payload);
    }

    private void handleInput(MessageEnvelope envelope) {
        GameSession session = hostSession;
        if (session == null || role != MatchRole.PLANT) {
            return;
        }
        try {
            MatchInputPayload payload = MAPPER.treeToValue(envelope.getPayload(), MatchInputPayload.class);
            if (payload == null || payload.getMatchId() == null || !payload.getMatchId().equals(matchId)) {
                return;
            }
            if (payload.getAction() == MatchInputAction.PLACE_ZOMBIE
                    && payload.getAlias() != null
                    && payload.getCol() != null
                    && payload.getRow() != null) {
                session.tryPlaceZombie(payload.getAlias(), payload.getCol(), payload.getRow());
                pushHostSnapshotNow();
            }
        } catch (Exception ignored) {
        }
    }

    private void handleState(MessageEnvelope envelope) {
        GameSession session = guestSession;
        if (session == null || role != MatchRole.ZOMBIE) {
            return;
        }
        try {
            MatchStatePayload payload = MAPPER.treeToValue(envelope.getPayload(), MatchStatePayload.class);
            if (payload == null || payload.getMatchId() == null || !payload.getMatchId().equals(matchId)) {
                return;
            }
            MatchSnapshotApplier.apply(session, payload);
            StateListener applied = stateListener;
            if (applied != null) {
                Gdx.app.postRunnable(applied::onStateApplied);
            }
        } catch (Exception ignored) {
        }
    }

    private void handleEnd(MessageEnvelope envelope) {
        try {
            MatchEndPayload payload = MAPPER.treeToValue(envelope.getPayload(), MatchEndPayload.class);
            Listener active = listener;
            if (active != null && payload != null) {
                active.onMatchEnd(payload);
            }
            clear();
        } catch (Exception ignored) {
        }
    }

    private void sendHostMatchEnd(GameSession session, String activeMatchId) {
        MatchWinner winner = session.getMatchResult() == MatchResult.WON
                ? MatchWinner.PLANT
                : MatchWinner.ZOMBIE;
        MatchEndReason reason = session.areAllIZombieBrainsEaten()
                ? MatchEndReason.BRAINS_EATEN
                : MatchEndReason.PLANT_TIMER;
        MatchEndPayload payload = new MatchEndPayload(activeMatchId, winner, reason);
        networkManager.trySend(MessageTypes.MATCH_END, payload);
        clear();
    }
}
