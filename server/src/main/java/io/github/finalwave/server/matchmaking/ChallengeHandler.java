package io.github.finalwave.server.matchmaking;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.finalwave.network.MessageEnvelope;
import io.github.finalwave.network.MessageTypes;
import io.github.finalwave.network.match.ChallengeFailPayload;
import io.github.finalwave.network.match.ChallengeFailReason;
import io.github.finalwave.network.match.ChallengeInvitePayload;
import io.github.finalwave.network.match.ChallengeRejectedPayload;
import io.github.finalwave.network.match.ChallengeRequest;
import io.github.finalwave.network.match.ChallengeResponsePayload;
import io.github.finalwave.network.match.ChallengeTimeoutPayload;
import io.github.finalwave.server.ClientHandler;
import io.github.finalwave.server.ServerContext;

import java.util.Optional;

public final class ChallengeHandler {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final ServerContext context;
    private final ClientHandler handler;

    public ChallengeHandler(ServerContext context, ClientHandler handler) {
        this.context = context;
        this.handler = handler;
    }

    public MessageEnvelope handleRequest(MessageEnvelope incoming) {
        Optional<String> challengerUsername = context.sessionRegistry().usernameFor(handler);
        if (challengerUsername.isEmpty()) {
            return fail(incoming, ChallengeFailReason.NOT_LOGGED_IN);
        }
        if (context.matchRegistry().isBusy(handler)) {
            return fail(incoming, ChallengeFailReason.BUSY);
        }
        try {
            ChallengeRequest request = MAPPER.treeToValue(incoming.getPayload(), ChallengeRequest.class);
            String targetUsername = request == null ? null : request.getTargetUsername();
            if (targetUsername == null || targetUsername.isBlank()) {
                return fail(incoming, ChallengeFailReason.USER_NOT_FOUND);
            }
            String trimmedTarget = targetUsername.trim();
            if (trimmedTarget.equals(challengerUsername.get())) {
                return fail(incoming, ChallengeFailReason.SELF_CHALLENGE);
            }
            Optional<ClientHandler> targetHandler = context.sessionRegistry().handlerFor(trimmedTarget);
            if (targetHandler.isEmpty()) {
                if (context.database().isUsernameTaken(trimmedTarget)) {
                    return fail(incoming, ChallengeFailReason.USER_OFFLINE);
                }
                return fail(incoming, ChallengeFailReason.USER_NOT_FOUND);
            }
            ClientHandler target = targetHandler.get();
            if (context.matchRegistry().isBusy(target)) {
                return fail(incoming, ChallengeFailReason.BUSY);
            }
            MatchRegistry.PendingInvite invite = context.matchRegistry().createInvite(
                    handler,
                    target,
                    challengerUsername.get(),
                    trimmedTarget,
                    this::handleTimeout);
            ChallengeInvitePayload invitePayload = new ChallengeInvitePayload(
                    invite.inviteId(), challengerUsername.get());
            target.push(new MessageEnvelope(
                    MessageTypes.CHALLENGE_INVITE,
                    null,
                    MAPPER.valueToTree(invitePayload)));
            return null;
        } catch (Exception exception) {
            return fail(incoming, ChallengeFailReason.USER_NOT_FOUND);
        }
    }

    public MessageEnvelope handleResponse(MessageEnvelope incoming) {
        try {
            ChallengeResponsePayload response = MAPPER.treeToValue(
                    incoming.getPayload(), ChallengeResponsePayload.class);
            if (response == null || response.getInviteId() == null) {
                return null;
            }
            Optional<MatchRegistry.PendingInvite> invite = context.matchRegistry()
                    .pendingInviteById(response.getInviteId());
            if (invite.isEmpty()) {
                return null;
            }
            MatchRegistry.PendingInvite pending = invite.get();
            if (pending.target() != handler) {
                return null;
            }
            if (response.isAccepted()) {
                context.matchRegistry().acceptInvite(response.getInviteId());
            } else {
                context.matchRegistry().rejectInvite(response.getInviteId());
                ChallengeRejectedPayload rejected = new ChallengeRejectedPayload(response.getInviteId());
                pending.challenger().push(new MessageEnvelope(
                        MessageTypes.CHALLENGE_REJECTED,
                        null,
                        MAPPER.valueToTree(rejected)));
            }
            return null;
        } catch (Exception exception) {
            return null;
        }
    }

    private void handleTimeout(String inviteId) {
        Optional<MatchRegistry.PendingInvite> invite = context.matchRegistry().pendingInviteById(inviteId);
        if (invite.isEmpty()) {
            return;
        }
        MatchRegistry.PendingInvite pending = invite.get();
        context.matchRegistry().timeoutInvite(inviteId);
        ChallengeTimeoutPayload payload = new ChallengeTimeoutPayload(inviteId);
        pending.challenger().push(new MessageEnvelope(
                MessageTypes.CHALLENGE_TIMEOUT,
                null,
                MAPPER.valueToTree(payload)));
    }

    private MessageEnvelope fail(MessageEnvelope incoming, ChallengeFailReason reason) {
        ChallengeFailPayload payload = new ChallengeFailPayload(reason);
        return new MessageEnvelope(
                MessageTypes.CHALLENGE_FAIL,
                incoming.getRequestId(),
                MAPPER.valueToTree(payload));
    }
}
