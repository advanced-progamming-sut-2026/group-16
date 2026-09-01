package io.github.finalwave.server.auth;

import io.github.finalwave.model.user.SecurityQuestion;
import io.github.finalwave.model.user.User;
import io.github.finalwave.network.auth.ChangePasswordRequest;
import io.github.finalwave.network.auth.PasswordChangeFailReason;
import io.github.finalwave.network.auth.PasswordChangeOkPayload;
import io.github.finalwave.network.auth.ResetPasswordRequest;
import io.github.finalwave.network.auth.SecurityQuestionLookupOkPayload;
import io.github.finalwave.server.ClientHandler;
import io.github.finalwave.server.ServerContext;
import io.github.finalwave.util.HashUtil;
import io.github.finalwave.util.RegistrationValidator;

public final class PasswordChangeService {
    private final ServerContext context;

    public PasswordChangeService(ServerContext context) {
        this.context = context;
    }

    public Result lookupSecurityQuestion(String username, String email) {
        if (blank(username) || blank(email)) {
            return Result.failure(PasswordChangeFailReason.INVALID_INPUT);
        }
        User user = context.database().getUser(username.trim());
        if (user == null || user.getEmail() == null
                || !user.getEmail().equalsIgnoreCase(email.trim())) {
            return Result.failure(PasswordChangeFailReason.USER_NOT_FOUND);
        }
        if (user.getSecurityQuestionId() <= 0) {
            return Result.failure(PasswordChangeFailReason.USER_NOT_FOUND);
        }
        SecurityQuestion question = SecurityQuestion.fromNumber(user.getSecurityQuestionId());
        if (question == null) {
            return Result.failure(PasswordChangeFailReason.SERVER_ERROR);
        }
        return Result.lookupSuccess(user.getSecurityQuestionId(), question.getText());
    }

    public Result reset(ResetPasswordRequest request) {
        if (request == null
                || blank(request.getUsername())
                || blank(request.getEmail())
                || blank(request.getSecurityAnswer())
                || blank(request.getNewPassword())) {
            return Result.failure(PasswordChangeFailReason.INVALID_INPUT);
        }
        if (!RegistrationValidator.isStrongPassword(request.getNewPassword())) {
            return Result.failure(PasswordChangeFailReason.WEAK_PASSWORD);
        }
        String username = request.getUsername().trim();
        User user = context.database().getUser(username);
        if (user == null || user.getEmail() == null
                || !user.getEmail().equalsIgnoreCase(request.getEmail().trim())) {
            return Result.failure(PasswordChangeFailReason.USER_NOT_FOUND);
        }
        if (!user.validateSecurityAnswer(request.getSecurityAnswer())) {
            return Result.failure(PasswordChangeFailReason.WRONG_SECURITY_ANSWER);
        }
        String newHash = HashUtil.hashSHA256(request.getNewPassword());
        if (newHash.equals(user.getPasswordHash())) {
            return Result.failure(PasswordChangeFailReason.SAME_PASSWORD);
        }
        context.database().updatePassword(username, newHash);
        return Result.success(username);
    }

    public Result change(ChangePasswordRequest request, ClientHandler handler) {
        String username = context.sessionRegistry().usernameFor(handler).orElse(null);
        if (username == null || username.isBlank()) {
            return Result.failure(PasswordChangeFailReason.AUTH_REQUIRED);
        }
        if (request == null || blank(request.getOldPassword()) || blank(request.getNewPassword())) {
            return Result.failure(PasswordChangeFailReason.INVALID_INPUT);
        }
        if (!RegistrationValidator.isStrongPassword(request.getNewPassword())) {
            return Result.failure(PasswordChangeFailReason.WEAK_PASSWORD);
        }
        User user = context.database().getUser(username);
        if (user == null) {
            return Result.failure(PasswordChangeFailReason.USER_NOT_FOUND);
        }
        if (!user.authenticate(request.getOldPassword())) {
            return Result.failure(PasswordChangeFailReason.BAD_CREDENTIALS);
        }
        String newHash = HashUtil.hashSHA256(request.getNewPassword());
        if (newHash.equals(user.getPasswordHash())) {
            return Result.failure(PasswordChangeFailReason.SAME_PASSWORD);
        }
        context.database().updatePassword(username, newHash);
        return Result.success(username);
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }

    public static final class Result {
        private final PasswordChangeOkPayload success;
        private final SecurityQuestionLookupOkPayload lookupSuccess;
        private final String failureReason;

        private Result(PasswordChangeOkPayload success,
                       SecurityQuestionLookupOkPayload lookupSuccess,
                       String failureReason) {
            this.success = success;
            this.lookupSuccess = lookupSuccess;
            this.failureReason = failureReason;
        }

        public static Result success(String username) {
            return new Result(new PasswordChangeOkPayload(username), null, null);
        }

        public static Result lookupSuccess(int securityQuestionNumber, String questionText) {
            return new Result(null, new SecurityQuestionLookupOkPayload(securityQuestionNumber, questionText), null);
        }

        public static Result failure(String reason) {
            return new Result(null, null, reason);
        }

        public boolean isSuccess() {
            return success != null || lookupSuccess != null;
        }

        public PasswordChangeOkPayload successPayload() {
            return success;
        }

        public SecurityQuestionLookupOkPayload lookupSuccessPayload() {
            return lookupSuccess;
        }

        public String failureReason() {
            return failureReason;
        }
    }
}
