package io.github.finalwave.server.auth;

import io.github.finalwave.model.user.User;
import io.github.finalwave.network.auth.LoginFailPayload;
import io.github.finalwave.network.auth.LoginFailReason;
import io.github.finalwave.network.auth.LoginOkPayload;
import io.github.finalwave.network.auth.LoginRequest;
import io.github.finalwave.profile.ProfileExporter;
import io.github.finalwave.server.ClientHandler;
import io.github.finalwave.server.ServerContext;

import java.util.Optional;

public final class LoginService {
    private final ServerContext context;

    public LoginService(ServerContext context) {
        this.context = context;
    }

    public LoginResult login(LoginRequest request, ClientHandler handler) {
        if (request == null
                || request.getUsername() == null
                || request.getUsername().isBlank()
                || request.getPassword() == null
                || request.getPassword().isBlank()) {
            return LoginResult.failure(LoginFailReason.INVALID_INPUT);
        }
        String username = request.getUsername().trim();
        User user = context.database().getUser(username);
        if (user == null || !user.authenticate(request.getPassword())) {
            return LoginResult.failure(LoginFailReason.BAD_CREDENTIALS);
        }
        Optional<String> bindFailure = context.sessionRegistry().tryBind(username, handler);
        if (bindFailure.isPresent()) {
            return LoginResult.failure(bindFailure.get());
        }
        try {
            LoginOkPayload payload = ProfileExporter.export(user, context.database().delegate());
            return LoginResult.success(payload);
        } catch (RuntimeException exception) {
            context.sessionRegistry().unbind(handler);
            return LoginResult.failure(LoginFailReason.SERVER_ERROR);
        }
    }

    public static final class LoginResult {
        private final LoginOkPayload successPayload;
        private final LoginFailPayload failurePayload;

        private LoginResult(LoginOkPayload successPayload, LoginFailPayload failurePayload) {
            this.successPayload = successPayload;
            this.failurePayload = failurePayload;
        }

        public static LoginResult success(LoginOkPayload payload) {
            return new LoginResult(payload, null);
        }

        public static LoginResult failure(String reason) {
            return new LoginResult(null, new LoginFailPayload(reason));
        }

        public boolean isSuccess() {
            return successPayload != null;
        }

        public LoginOkPayload successPayload() {
            return successPayload;
        }

        public LoginFailPayload failurePayload() {
            return failurePayload;
        }
    }
}
