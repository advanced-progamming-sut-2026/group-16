package io.github.finalwave.server.auth;

import io.github.finalwave.network.auth.LoginFailReason;
import io.github.finalwave.network.auth.LoginOkPayload;
import io.github.finalwave.network.sync.ResumePayload;
import io.github.finalwave.network.sync.SyncFailReason;
import io.github.finalwave.profile.ProfileExporter;
import io.github.finalwave.model.user.User;
import io.github.finalwave.server.ClientHandler;
import io.github.finalwave.server.ServerContext;

import java.util.Optional;

public final class ResumeService {
    private final ServerContext context;

    public ResumeService(ServerContext context) {
        this.context = context;
    }

    public ResumeResult resume(ResumePayload request, ClientHandler handler) {
        if (request == null
                || request.getUsername() == null
                || request.getUsername().isBlank()
                || request.getPasswordHash() == null
                || request.getPasswordHash().isBlank()) {
            return ResumeResult.failure(SyncFailReason.VALIDATION);
        }
        String username = request.getUsername().trim();
        User user = context.database().getUser(username);
        if (user == null || !user.getPasswordHash().equals(request.getPasswordHash())) {
            return ResumeResult.failure(SyncFailReason.BAD_CREDENTIALS);
        }
        Optional<String> bindFailure = context.sessionRegistry().tryBind(username, handler);
        if (bindFailure.isPresent()) {
            String reason = bindFailure.get();
            if (LoginFailReason.ALREADY_LOGGED_IN.equals(reason)) {
                return ResumeResult.failure(SyncFailReason.ALREADY_LOGGED_IN);
            }
            return ResumeResult.failure(SyncFailReason.VALIDATION);
        }
        try {
            LoginOkPayload payload = ProfileExporter.export(user, context.database().delegate());
            return ResumeResult.success(payload);
        } catch (RuntimeException exception) {
            context.sessionRegistry().unbind(handler);
            return ResumeResult.failure(SyncFailReason.SERVER_ERROR);
        }
    }

    public static final class ResumeResult {
        private final LoginOkPayload successPayload;
        private final String failureReason;

        private ResumeResult(LoginOkPayload successPayload, String failureReason) {
            this.successPayload = successPayload;
            this.failureReason = failureReason;
        }

        public static ResumeResult success(LoginOkPayload payload) {
            return new ResumeResult(payload, null);
        }

        public static ResumeResult failure(String reason) {
            return new ResumeResult(null, reason);
        }

        public boolean isSuccess() {
            return successPayload != null;
        }

        public LoginOkPayload successPayload() {
            return successPayload;
        }

        public String failureReason() {
            return failureReason;
        }
    }
}
