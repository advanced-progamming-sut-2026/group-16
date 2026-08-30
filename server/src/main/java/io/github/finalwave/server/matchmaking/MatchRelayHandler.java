package io.github.finalwave.server.matchmaking;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.finalwave.network.MessageEnvelope;
import io.github.finalwave.network.MessageTypes;
import io.github.finalwave.network.match.MatchEndPayload;
import io.github.finalwave.server.ClientHandler;
import io.github.finalwave.server.ServerContext;

import java.util.Optional;

public final class MatchRelayHandler {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final ServerContext context;
    private final ClientHandler handler;

    public MatchRelayHandler(ServerContext context, ClientHandler handler) {
        this.context = context;
        this.handler = handler;
    }

    public MessageEnvelope handleInput(MessageEnvelope incoming) {
        return relay(incoming, MessageTypes.MATCH_INPUT, true);
    }

    public MessageEnvelope handleState(MessageEnvelope incoming) {
        return relay(incoming, MessageTypes.MATCH_STATE, false);
    }

    public MessageEnvelope handleEnd(MessageEnvelope incoming) {
        try {
            MatchEndPayload payload = MAPPER.treeToValue(incoming.getPayload(), MatchEndPayload.class);
            if (payload == null || payload.getMatchId() == null) {
                return null;
            }
            if (!context.matchRegistry().isHost(handler, payload.getMatchId())) {
                return null;
            }
            Optional<ClientHandler> partner = context.matchRegistry().partnerFor(handler, payload.getMatchId());
            partner.ifPresent(other -> other.push(clonePush(MessageTypes.MATCH_END, incoming.getPayload())));
            context.matchRegistry().endMatch(payload.getMatchId());
            return null;
        } catch (Exception exception) {
            return null;
        }
    }

    private MessageEnvelope relay(MessageEnvelope incoming, String type, boolean guestOnly) {
        try {
            String matchId = extractMatchId(incoming.getPayload());
            if (matchId == null || matchId.isBlank()) {
                return null;
            }
            if (guestOnly) {
                if (!context.matchRegistry().isGuest(handler, matchId)) {
                    return null;
                }
            } else if (!context.matchRegistry().isHost(handler, matchId)) {
                return null;
            }
            Optional<ClientHandler> partner = context.matchRegistry().partnerFor(handler, matchId);
            partner.ifPresent(other -> other.push(clonePush(type, incoming.getPayload())));
            return null;
        } catch (Exception exception) {
            return null;
        }
    }

    private static String extractMatchId(JsonNode payload) {
        if (payload == null || payload.isNull() || !payload.has("matchId")) {
            return null;
        }
        return payload.get("matchId").asText();
    }

    private static MessageEnvelope clonePush(String type, JsonNode payload) {
        return new MessageEnvelope(type, null, payload);
    }
}
