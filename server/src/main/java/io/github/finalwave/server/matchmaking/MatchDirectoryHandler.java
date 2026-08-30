package io.github.finalwave.server.matchmaking;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.finalwave.model.user.User;
import io.github.finalwave.network.MessageEnvelope;
import io.github.finalwave.network.MessageTypes;
import io.github.finalwave.network.match.ListMatchUsersResponse;
import io.github.finalwave.network.match.MatchUserEntry;
import io.github.finalwave.server.ClientHandler;
import io.github.finalwave.server.ServerContext;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class MatchDirectoryHandler {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final ServerContext context;
    private final ClientHandler handler;

    public MatchDirectoryHandler(ServerContext context, ClientHandler handler) {
        this.context = context;
        this.handler = handler;
    }

    public MessageEnvelope listUsers(MessageEnvelope incoming) {
        Optional<String> selfUsername = context.sessionRegistry().usernameFor(handler);
        boolean selfOnline = selfUsername.isPresent();
        boolean selfBusy = context.matchRegistry().isBusy(handler);
        List<MatchUserEntry> entries = new ArrayList<>();
        for (User user : context.database().getAllUsers()) {
            if (user == null || user.getUsername() == null || user.getUsername().isBlank()) {
                continue;
            }
            String username = user.getUsername();
            boolean online = context.sessionRegistry().isOnline(username);
            boolean busy = context.sessionRegistry().handlerFor(username)
                    .map(context.matchRegistry()::isBusy)
                    .orElse(false);
            entries.add(new MatchUserEntry(username, online, busy));
        }
        entries.sort(Comparator
                .comparing(MatchUserEntry::isOnline).reversed()
                .thenComparing(entry -> entry.getUsername().toLowerCase()));
        ListMatchUsersResponse response = new ListMatchUsersResponse(
                selfUsername.orElse(""),
                selfOnline,
                selfBusy,
                entries);
        return new MessageEnvelope(
                MessageTypes.LIST_MATCH_USERS_OK,
                incoming.getRequestId(),
                MAPPER.valueToTree(response));
    }

    public MessageEnvelope resetMatchmaking(MessageEnvelope incoming) {
        context.randomQueue().remove(handler);
        context.matchRegistry().onDisconnect(handler);
        return new MessageEnvelope(
                MessageTypes.MATCHMAKING_RESET_OK,
                incoming.getRequestId(),
                MAPPER.valueToTree(Map.of()));
    }
}
