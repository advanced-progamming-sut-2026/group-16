package io.github.finalwave.server.matchmaking;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.finalwave.network.MessageEnvelope;
import io.github.finalwave.network.MessageTypes;
import io.github.finalwave.server.ClientHandler;
import io.github.finalwave.server.ServerContext;

public final class MatchDirectoryBroadcaster {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private MatchDirectoryBroadcaster() {
    }

    public static void broadcast(ServerContext context) {
        for (ClientHandler handler : context.sessionRegistry().handlers()) {
            handler.push(new MessageEnvelope(
                    MessageTypes.MATCH_USERS_UPDATED,
                    null,
                    MAPPER.valueToTree(MatchDirectoryHandler.snapshot(context, handler))));
        }
    }
}
