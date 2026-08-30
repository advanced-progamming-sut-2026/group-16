package io.github.finalwave.server.presence;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.finalwave.network.MessageEnvelope;
import io.github.finalwave.network.MessageTypes;
import io.github.finalwave.network.match.CheckUserStatusRequest;
import io.github.finalwave.network.match.CheckUserStatusResponse;
import io.github.finalwave.network.match.UserStatus;

final class UserStatusHandlerTestSupport {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private UserStatusHandlerTestSupport() {
    }

    static MessageEnvelope request(String username) {
        return new MessageEnvelope(
                MessageTypes.CHECK_USER_STATUS,
                "req-1",
                MAPPER.valueToTree(new CheckUserStatusRequest(username)));
    }

    static UserStatus parseStatus(MessageEnvelope envelope) throws Exception {
        CheckUserStatusResponse response = MAPPER.treeToValue(
                envelope.getPayload(), CheckUserStatusResponse.class);
        return response.getStatus();
    }
}
