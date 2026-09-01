package io.github.finalwave.server.matchmaking;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.finalwave.network.MessageEnvelope;
import io.github.finalwave.network.MessageTypes;
import io.github.finalwave.network.match.MatchRole;
import io.github.finalwave.network.match.MatchStartPayload;
import io.github.finalwave.network.match.MatchWinner;
import io.github.finalwave.server.ClientHandler;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.function.Consumer;
import java.util.concurrent.TimeUnit;

public final class MatchRegistry {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final long INVITE_TIMEOUT_SECONDS = 30L;
    private static final int NETWORKED_STAGE_INDEX = 1;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r, "match-invite-timeout");
        thread.setDaemon(true);
        return thread;
    });

    private final Map<String, PendingInvite> pendingInvites = new ConcurrentHashMap<>();
    private final Map<String, ActiveMatch> activeMatches = new ConcurrentHashMap<>();
    private final Map<ClientHandler, String> handlerToInviteId = new ConcurrentHashMap<>();
    private final Map<ClientHandler, String> handlerToMatchId = new ConcurrentHashMap<>();

    public record PendingInvite(
            String inviteId,
            ClientHandler challenger,
            ClientHandler target,
            String challengerUsername,
            String targetUsername,
            ScheduledFuture<?> timeoutTask) {
    }

    public record ActiveMatch(
            String matchId,
            ClientHandler host,
            ClientHandler guest,
            String hostUsername,
            String guestUsername) {
    }

    public boolean isBusy(ClientHandler handler) {
        return handlerToInviteId.containsKey(handler) || handlerToMatchId.containsKey(handler);
    }

    public Optional<PendingInvite> pendingInviteById(String inviteId) {
        return Optional.ofNullable(pendingInvites.get(inviteId));
    }

    public Optional<PendingInvite> pendingInviteForHandler(ClientHandler handler) {
        String inviteId = handlerToInviteId.get(handler);
        if (inviteId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(pendingInvites.get(inviteId));
    }

    public Optional<ActiveMatch> activeMatchForHandler(ClientHandler handler) {
        String matchId = handlerToMatchId.get(handler);
        if (matchId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(activeMatches.get(matchId));
    }

    public PendingInvite createInvite(
            ClientHandler challenger,
            ClientHandler target,
            String challengerUsername,
            String targetUsername,
            Consumer<String> onTimeout) {
        String inviteId = UUID.randomUUID().toString();
        ScheduledFuture<?> timeoutTask = scheduler.schedule(
                () -> onTimeout.accept(inviteId),
                INVITE_TIMEOUT_SECONDS,
                TimeUnit.SECONDS);
        PendingInvite invite = new PendingInvite(
                inviteId, challenger, target, challengerUsername, targetUsername, timeoutTask);
        pendingInvites.put(inviteId, invite);
        handlerToInviteId.put(challenger, inviteId);
        handlerToInviteId.put(target, inviteId);
        return invite;
    }

    public Optional<ActiveMatch> acceptInvite(String inviteId) {
        PendingInvite invite = pendingInvites.remove(inviteId);
        if (invite == null) {
            return Optional.empty();
        }
        cancelTimeout(invite);
        handlerToInviteId.remove(invite.challenger());
        handlerToInviteId.remove(invite.target());
        return Optional.of(createMatch(
                invite.challenger(),
                invite.target(),
                invite.challengerUsername(),
                invite.targetUsername()));
    }

    public void rejectInvite(String inviteId) {
        PendingInvite invite = pendingInvites.remove(inviteId);
        if (invite == null) {
            return;
        }
        cancelTimeout(invite);
        handlerToInviteId.remove(invite.challenger());
        handlerToInviteId.remove(invite.target());
    }

    public void timeoutInvite(String inviteId) {
        PendingInvite invite = pendingInvites.remove(inviteId);
        if (invite == null) {
            return;
        }
        handlerToInviteId.remove(invite.challenger());
        handlerToInviteId.remove(invite.target());
    }

    public ActiveMatch createRandomMatch(
            ClientHandler host,
            ClientHandler guest,
            String hostUsername,
            String guestUsername) {
        return createMatch(host, guest, hostUsername, guestUsername);
    }

    private ActiveMatch createMatch(
            ClientHandler host,
            ClientHandler guest,
            String hostUsername,
            String guestUsername) {
        String matchId = UUID.randomUUID().toString();
        ActiveMatch match = new ActiveMatch(matchId, host, guest, hostUsername, guestUsername);
        activeMatches.put(matchId, match);
        handlerToMatchId.put(host, matchId);
        handlerToMatchId.put(guest, matchId);
        pushMatchStart(host, matchId, guestUsername, MatchRole.PLANT);
        pushMatchStart(guest, matchId, hostUsername, MatchRole.ZOMBIE);
        return match;
    }

    public void clearPendingInvites(ClientHandler handler) {
        String inviteId = handlerToInviteId.remove(handler);
        if (inviteId == null) {
            return;
        }
        PendingInvite invite = pendingInvites.remove(inviteId);
        if (invite == null) {
            return;
        }
        cancelTimeout(invite);
        ClientHandler other = invite.challenger() == handler ? invite.target() : invite.challenger();
        handlerToInviteId.remove(other);
    }

    public void onDisconnect(ClientHandler handler) {
        clearPendingInvites(handler);
        String matchId = handlerToMatchId.remove(handler);
        if (matchId != null) {
            ActiveMatch match = activeMatches.remove(matchId);
            if (match != null) {
                ClientHandler partner = match.host() == handler ? match.guest() : match.host();
                handlerToMatchId.remove(partner);
                MatchWinner winner = match.host() == handler ? MatchWinner.ZOMBIE : MatchWinner.PLANT;
                MatchDisconnectNotifier.notifyOpponentDisconnected(partner, matchId, winner);
            }
        }
    }

    public Optional<ClientHandler> partnerFor(ClientHandler handler, String matchId) {
        ActiveMatch match = activeMatches.get(matchId);
        if (match == null) {
            return Optional.empty();
        }
        if (match.host() == handler) {
            return Optional.of(match.guest());
        }
        if (match.guest() == handler) {
            return Optional.of(match.host());
        }
        return Optional.empty();
    }

    public boolean isHost(ClientHandler handler, String matchId) {
        ActiveMatch match = activeMatches.get(matchId);
        return match != null && match.host() == handler;
    }

    public boolean isGuest(ClientHandler handler, String matchId) {
        ActiveMatch match = activeMatches.get(matchId);
        return match != null && match.guest() == handler;
    }

    public void endMatch(String matchId) {
        ActiveMatch match = activeMatches.remove(matchId);
        if (match == null) {
            return;
        }
        handlerToMatchId.remove(match.host());
        handlerToMatchId.remove(match.guest());
    }

    private static void pushMatchStart(
            ClientHandler handler,
            String matchId,
            String opponentUsername,
            MatchRole role) {
        MatchStartPayload payload = new MatchStartPayload(matchId, opponentUsername, role, NETWORKED_STAGE_INDEX);
        payload.setPhase(io.github.finalwave.model.minigame.izombie.IZombieDuelCatalog.PHASE_PICKING);
        payload.setPickSeconds(io.github.finalwave.model.minigame.izombie.IZombieDuelCatalog.PICK_SECONDS);
        payload.setRoundSeconds(io.github.finalwave.model.minigame.izombie.IZombieDuelCatalog.ROUND_SECONDS);
        payload.setPool(java.util.List.of());
        if (role == MatchRole.ZOMBIE) {
            payload.setSlots(io.github.finalwave.model.minigame.izombie.IZombieDuelCatalog.ZOMBIE_SLOTS);
        } else {
            payload.setSlots(io.github.finalwave.model.minigame.izombie.IZombieDuelCatalog.PLANT_SLOTS);
        }
        handler.push(new MessageEnvelope(MessageTypes.MATCH_START, null, MAPPER.valueToTree(payload)));
    }

    private static void cancelTimeout(PendingInvite invite) {
        if (invite.timeoutTask() != null) {
            invite.timeoutTask().cancel(false);
        }
    }
}
