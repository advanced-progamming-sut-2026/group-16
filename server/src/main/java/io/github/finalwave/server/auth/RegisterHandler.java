package io.github.finalwave.server.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.finalwave.network.MessageEnvelope;
import io.github.finalwave.network.MessageTypes;
import io.github.finalwave.network.auth.RegisterFailPayload;
import io.github.finalwave.network.auth.RegisterFailReason;
import io.github.finalwave.network.auth.RegisterOkPayload;
import io.github.finalwave.network.auth.RegisterRequest;
import io.github.finalwave.server.ClientHandler;
import io.github.finalwave.server.ServerContext;

import java.util.Optional;

public final class RegisterHandler {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final RegisterService registerService;
    private final ServerContext context;
    private final ClientHandler handler;

    public RegisterHandler(ServerContext context, ClientHandler handler) {
        this.context = context;
        this.handler = handler;
        this.registerService = new RegisterService(context.database());
    }

    public MessageEnvelope handle(MessageEnvelope incoming) {
        try {
            RegisterRequest request = MAPPER.treeToValue(incoming.getPayload(), RegisterRequest.class);
            RegisterService.RegisterResult result = registerService.register(request);
            if (result.isSuccess()) {
                RegisterOkPayload payload = result.successPayload();
                Optional<String> bindFailure = context.sessionRegistry().tryBind(payload.getUsername(), handler);
                if (bindFailure.isPresent()) {
                    return new MessageEnvelope(
                            MessageTypes.REGISTER_FAIL,
                            incoming.getRequestId(),
                            MAPPER.valueToTree(new RegisterFailPayload(bindFailure.get()))
                    );
                }
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
            RegisterFailPayload payload = new RegisterFailPayload(RegisterFailReason.SERVER_ERROR);
            return new MessageEnvelope(
                    MessageTypes.REGISTER_FAIL,
                    incoming.getRequestId(),
                    MAPPER.valueToTree(payload)
            );
        }
    }
}
