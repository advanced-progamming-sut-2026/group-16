package io.github.finalwave.server.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.finalwave.network.MessageEnvelope;
import io.github.finalwave.network.MessageTypes;
import io.github.finalwave.network.auth.RegisterFailPayload;
import io.github.finalwave.network.auth.RegisterOkPayload;
import io.github.finalwave.network.auth.RegisterRequest;
import io.github.finalwave.server.db.ServerDatabase;

public final class RegisterHandler {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final RegisterService registerService;

    public RegisterHandler(ServerDatabase database) {
        this.registerService = new RegisterService(database);
    }

    public MessageEnvelope handle(MessageEnvelope incoming) {
        try {
            RegisterRequest request = MAPPER.treeToValue(incoming.getPayload(), RegisterRequest.class);
            RegisterService.RegisterResult result = registerService.register(request);
            if (result.isSuccess()) {
                RegisterOkPayload payload = result.successPayload();
                return new MessageEnvelope(
                        MessageTypes.REGISTER_OK,
                        incoming.getRequestId(),
                        MAPPER.valueToTree(payload)
                );
            }
            RegisterFailPayload payload = result.failurePayload();
            return new MessageEnvelope(
                    MessageTypes.REGISTER_FAIL,
                    incoming.getRequestId(),
                    MAPPER.valueToTree(payload)
            );
        } catch (Exception exception) {
            RegisterFailPayload payload = new RegisterFailPayload(
                    io.github.finalwave.network.auth.RegisterFailReason.SERVER_ERROR
            );
            return new MessageEnvelope(
                    MessageTypes.REGISTER_FAIL,
                    incoming.getRequestId(),
                    MAPPER.valueToTree(payload)
            );
        }
    }
}
