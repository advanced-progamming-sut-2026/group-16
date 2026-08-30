package io.github.finalwave.server.matchmaking;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.finalwave.network.MessageEnvelope;
import io.github.finalwave.network.MessageTypes;
import io.github.finalwave.network.match.MatchEndPayload;
import io.github.finalwave.network.match.MatchEndReason;
import io.github.finalwave.network.match.MatchWinner;
import io.github.finalwave.server.ClientHandler;

public final class MatchDisconnectNotifier {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private MatchDisconnectNotifier() {
    }

    public static void notifyOpponentDisconnected(ClientHandler partner, String matchId, MatchWinner winner) {
        if (partner == null) {
            return;
        }
        MatchEndPayload payload = new MatchEndPayload(matchId, winner, MatchEndReason.OPPONENT_DISCONNECTED);
        partner.push(new MessageEnvelope(MessageTypes.MATCH_END, null, MAPPER.valueToTree(payload)));
    }
}
