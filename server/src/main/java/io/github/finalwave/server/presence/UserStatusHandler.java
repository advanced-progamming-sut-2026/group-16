package io.github.finalwave.server.presence;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.finalwave.network.MessageEnvelope;
import io.github.finalwave.network.MessageTypes;
import io.github.finalwave.network.match.CheckUserStatusRequest;
import io.github.finalwave.network.match.CheckUserStatusResponse;
import io.github.finalwave.network.match.UserStatus;
import io.github.finalwave.server.ClientHandler;
import io.github.finalwave.server.ServerContext;

public final class UserStatusHandler {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final ServerContext context;
    private final ClientHandler handler;

    public UserStatusHandler(ServerContext context, ClientHandler handler) {
        this.context = context;
        this.handler = handler;
    }

    public MessageEnvelope handle(MessageEnvelope incoming) {
        try {
            CheckUserStatusRequest request = MAPPER.treeToValue(incoming.getPayload(), CheckUserStatusRequest.class);
            UserStatus status = resolveStatus(request == null ? null : request.getUsername());
            CheckUserStatusResponse response = new CheckUserStatusResponse(status);
            return new MessageEnvelope(
                    MessageTypes.CHECK_USER_STATUS_OK,
                    incoming.getRequestId(),
                    MAPPER.valueToTree(response));
        } catch (Exception exception) {
            CheckUserStatusResponse response = new CheckUserStatusResponse(UserStatus.NOT_FOUND);
            return new MessageEnvelope(
                    MessageTypes.CHECK_USER_STATUS_OK,
                    incoming.getRequestId(),
                    MAPPER.valueToTree(response));
        }
    }

    private UserStatus resolveStatus(String username) {
        if (username == null || username.isBlank()) {
            return UserStatus.NOT_FOUND;
        }
        String trimmed = username.trim();
        if (context.sessionRegistry().isOnline(trimmed)) {
            return UserStatus.ONLINE;
        }
        if (context.database().isUsernameTaken(trimmed)) {
            return UserStatus.OFFLINE;
        }
        return UserStatus.NOT_FOUND;
    }
}
