package io.github.finalwave.server.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.finalwave.network.MessageEnvelope;
import io.github.finalwave.network.MessageTypes;
import io.github.finalwave.network.auth.LoginFailPayload;
import io.github.finalwave.network.auth.LoginFailReason;
import io.github.finalwave.network.auth.LoginOkPayload;
import io.github.finalwave.network.auth.LoginRequest;
import io.github.finalwave.server.ClientHandler;

public final class LoginHandler {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final LoginService loginService;
    private final ClientHandler handler;

    public LoginHandler(LoginService loginService, ClientHandler handler) {
        this.loginService = loginService;
        this.handler = handler;
    }

    public MessageEnvelope handle(MessageEnvelope incoming) {
        try {
            LoginRequest request = MAPPER.treeToValue(incoming.getPayload(), LoginRequest.class);
            LoginService.LoginResult result = loginService.login(request, handler);
            if (result.isSuccess()) {
                LoginOkPayload payload = result.successPayload();
                return new MessageEnvelope(
                        MessageTypes.LOGIN_OK,
                        incoming.getRequestId(),
                        MAPPER.valueToTree(payload)
                );
            }
            LoginFailPayload payload = result.failurePayload();
            return new MessageEnvelope(
                    MessageTypes.LOGIN_FAIL,
                    incoming.getRequestId(),
                    MAPPER.valueToTree(payload)
            );
        } catch (Exception exception) {
            LoginFailPayload payload = new LoginFailPayload(LoginFailReason.SERVER_ERROR);
            return new MessageEnvelope(
                    MessageTypes.LOGIN_FAIL,
                    incoming.getRequestId(),
                    MAPPER.valueToTree(payload)
            );
        }
    }
}
