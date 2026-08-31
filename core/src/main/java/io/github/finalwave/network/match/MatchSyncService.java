package io.github.finalwave.network.match;

import com.badlogic.gdx.Gdx;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.MatchResult;
import io.github.finalwave.network.MessageEnvelope;
import io.github.finalwave.network.MessageTypes;
import io.github.finalwave.network.NetworkManager;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class MatchSyncService {

    public interface Listener {
        void onMatchEnd(MatchEndPayload payload);
    }

    public interface StateListener {
        void onStateApplied();
    }

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final long SNAPSHOT_INTERVAL_MS = 100L;
    private static final long GUEST_RESYNC_INTERVAL_MS = 350L;

    private final NetworkManager networkManager;
    private volatile Listener listener;
    private volatile StateListener stateListener;
    private volatile Consumer<List<String>> guestPicksListener;
    private volatile Consumer<MatchReactionPayload> reactionListener;
    private volatile String matchId;
    private volatile MatchRole role;
    private volatile GameSession hostSession;
    private volatile GameSession guestSession;
    private volatile MatchStatePayload pendingGuestState;
    private volatile MatchStatePayload lastGuestState;
    private volatile long lastSnapshotMillis;
    private volatile long lastGuestSnapshotMillis;
    private volatile long lastGuestResyncRequestMillis;
    private volatile long lastAppliedGuestTick = -1L;
    private final List<MatchInputPayload> pendingHostInputs = new ArrayList<>();

    public MatchSyncService(NetworkManager networkManager) {
        this.networkManager = networkManager;
        networkManager.registerListener(MessageTypes.MATCH_INPUT, this::handleInput);
        networkManager.registerListener(MessageTypes.MATCH_STATE, this::handleState);
        networkManager.registerListener(MessageTypes.MATCH_END, this::handleEnd);
        networkManager.registerListener(MessageTypes.MATCH_REACTION, this::handleReaction);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void setStateListener(StateListener stateListener) {
        this.stateListener = stateListener;
    }

    public void setGuestPicksListener(Consumer<List<String>> guestPicksListener) {
        this.guestPicksListener = guestPicksListener;
    }

    public void setReactionListener(Consumer<MatchReactionPayload> reactionListener) {
        this.reactionListener = reactionListener;
    }

    public void registerMatch(MatchStartPayload start, GameSession session) {
        if (start == null) {
            return;
        }
        matchId = start.getMatchId();
        role = start.getYourRole();
        lastAppliedGuestTick = -1L;
        lastGuestSnapshotMillis = 0L;
        lastGuestResyncRequestMillis = 0L;
        pendingHostInputs.clear();
        if (role == MatchRole.PLANT) {
            hostSession = session;
            guestSession = null;
            pendingGuestState = null;
            lastSnapshotMillis = 0L;
            drainPendingHostInputs();
            pushHostSnapshotNow();
            return;
        }
        guestSession = session;
        hostSession = null;
        lastSnapshotMillis = 0L;
        applyPendingGuestState();
        sendGuestReady();
    }

    public void clear() {
        matchId = null;
        role = null;
        hostSession = null;
        guestSession = null;
        pendingGuestState = null;
        lastGuestState = null;
        pendingHostInputs.clear();
        lastAppliedGuestTick = -1L;
        lastGuestSnapshotMillis = 0L;
        lastGuestResyncRequestMillis = 0L;
        guestPicksListener = null;
        reactionListener = null;
    }

    public MatchStatePayload lastGuestState() {
        return lastGuestState;
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

    public void tickGuest() {
        if (role != MatchRole.ZOMBIE || matchId == null) {
            return;
        }
        long now = System.currentTimeMillis();
        if (lastGuestSnapshotMillis > 0L && now - lastGuestSnapshotMillis < GUEST_RESYNC_INTERVAL_MS) {
            return;
        }
        if (now - lastGuestResyncRequestMillis < GUEST_RESYNC_INTERVAL_MS) {
            return;
        }
        lastGuestResyncRequestMillis = now;
        sendGuestReady();
    }

    public void pushHostSnapshotNow() {
        sendHostSnapshot(true);
    }

    public void sendGuestReady() {
        String activeMatchId = matchId;
        if (activeMatchId == null || role != MatchRole.ZOMBIE) {
            return;
        }
        networkManager.trySend(MessageTypes.MATCH_INPUT, MatchInputPayload.guestReady(activeMatchId));
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

    public void sendGuestPicks(List<String> picks) {
        String activeMatchId = matchId;
        if (activeMatchId == null || role != MatchRole.ZOMBIE) {
            return;
        }
        networkManager.trySend(MessageTypes.MATCH_INPUT, MatchInputPayload.submitPicks(activeMatchId, picks));
    }

    public void sendReaction(String kind, int index, String fromUsername) {
        String activeMatchId = matchId;
        if (activeMatchId == null) {
            return;
        }
        MatchReactionPayload payload = new MatchReactionPayload(activeMatchId, kind, index, fromUsername);
        networkManager.trySend(MessageTypes.MATCH_REACTION, payload);
    }

    public void sendForfeit() {
        String activeMatchId = matchId;
        if (activeMatchId == null) {
            return;
        }
        MatchInputPayload payload = new MatchInputPayload();
        payload.setMatchId(activeMatchId);
        payload.setAction(MatchInputAction.FORFEIT);
        networkManager.trySend(MessageTypes.MATCH_INPUT, payload);
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

    private void handleInput(MessageEnvelope envelope) {
        try {
            MatchInputPayload payload = MAPPER.treeToValue(envelope.getPayload(), MatchInputPayload.class);
            if (payload == null || payload.getMatchId() == null) {
                return;
            }
            String activeMatchId = matchId;
            if (activeMatchId != null && !activeMatchId.equals(payload.getMatchId())) {
                return;
            }
            if (payload.getAction() == MatchInputAction.FORFEIT) {
                return;
            }
            GameSession session = hostSession;
            if (session == null || role != MatchRole.PLANT) {
                if (payload.getAction() == MatchInputAction.PLACE_ZOMBIE
                        || payload.getAction() == MatchInputAction.GUEST_READY
                        || payload.getAction() == MatchInputAction.SUBMIT_PICKS) {
                    pendingHostInputs.add(payload);
                }
                return;
            }
            processHostInput(session, payload);
        } catch (Exception exception) {
            Gdx.app.error("MatchSyncService", "Failed to handle match input", exception);
        }
    }

    private void drainPendingHostInputs() {
        GameSession session = hostSession;
        String activeMatchId = matchId;
        if (session == null || activeMatchId == null || pendingHostInputs.isEmpty()) {
            return;
        }
        List<MatchInputPayload> queued = List.copyOf(pendingHostInputs);
        pendingHostInputs.clear();
        for (MatchInputPayload payload : queued) {
            if (payload != null && activeMatchId.equals(payload.getMatchId())) {
                processHostInput(session, payload);
            }
        }
    }

    private void processHostInput(GameSession session, MatchInputPayload payload) {
        if (payload.getAction() == MatchInputAction.GUEST_READY) {
            pushHostSnapshotNow();
            return;
        }
        if (payload.getAction() == MatchInputAction.SUBMIT_PICKS) {
            Consumer<List<String>> picksListener = guestPicksListener;
            List<String> picks = payload.getPicks() == null ? List.of() : List.copyOf(payload.getPicks());
            if (picksListener != null) {
                Gdx.app.postRunnable(() -> picksListener.accept(picks));
            }
            return;
        }
        if (payload.getAction() == MatchInputAction.PLACE_ZOMBIE
                && payload.getAlias() != null
                && payload.getCol() != null
                && payload.getRow() != null) {
            session.tryPlaceZombie(payload.getAlias(), payload.getCol(), payload.getRow());
            pushHostSnapshotNow();
        }
    }

    private void handleState(MessageEnvelope envelope) {
        try {
            MatchStatePayload payload = MAPPER.treeToValue(envelope.getPayload(), MatchStatePayload.class);
            if (payload == null || payload.getMatchId() == null) {
                return;
            }
            String activeMatchId = matchId;
            GameSession session = guestSession;
            if (session == null || role != MatchRole.ZOMBIE) {
                if (activeMatchId == null || activeMatchId.equals(payload.getMatchId())) {
                    pendingGuestState = payload;
                }
                return;
            }
            if (!payload.getMatchId().equals(activeMatchId)) {
                return;
            }
            applyGuestState(payload);
        } catch (Exception exception) {
            Gdx.app.error("MatchSyncService", "Failed to handle match state", exception);
        }
    }

    private void applyPendingGuestState() {
        GameSession session = guestSession;
        MatchStatePayload pending = pendingGuestState;
        String activeMatchId = matchId;
        if (session == null || pending == null || activeMatchId == null
                || !activeMatchId.equals(pending.getMatchId())) {
            return;
        }
        pendingGuestState = null;
        applyGuestState(pending);
    }

    private void applyGuestState(MatchStatePayload payload) {
        GameSession session = guestSession;
        if (session == null || payload == null) {
            return;
        }
        if (payload.getTick() < lastAppliedGuestTick) {
            return;
        }
        lastAppliedGuestTick = payload.getTick();
        lastGuestSnapshotMillis = System.currentTimeMillis();
        lastGuestState = payload;
        MatchSnapshotApplier.apply(session, payload);
        notifyStateListener();
    }

    private void notifyStateListener() {
        StateListener applied = stateListener;
        if (applied != null) {
            Gdx.app.postRunnable(applied::onStateApplied);
        }
    }

    private void handleReaction(MessageEnvelope envelope) {
        try {
            MatchReactionPayload payload = MAPPER.treeToValue(envelope.getPayload(), MatchReactionPayload.class);
            if (payload == null || payload.getMatchId() == null) {
                return;
            }
            String activeMatchId = matchId;
            if (activeMatchId == null || !activeMatchId.equals(payload.getMatchId())) {
                return;
            }
            Consumer<MatchReactionPayload> active = reactionListener;
            if (active != null) {
                Gdx.app.postRunnable(() -> active.accept(payload));
            }
        } catch (Exception exception) {
            Gdx.app.error("MatchSyncService", "Failed to handle match reaction", exception);
        }
    }

    private void handleEnd(MessageEnvelope envelope) {
        try {
            MatchEndPayload payload = MAPPER.treeToValue(envelope.getPayload(), MatchEndPayload.class);
            if (payload == null || payload.getMatchId() == null) {
                return;
            }
            String activeMatchId = matchId;
            if (activeMatchId == null || !activeMatchId.equals(payload.getMatchId())) {
                return;
            }
            Listener active = listener;
            if (active != null) {
                active.onMatchEnd(payload);
            }
            clear();
        } catch (Exception exception) {
            Gdx.app.error("MatchSyncService", "Failed to handle match end", exception);
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
