package io.github.finalwave.server.auth;

import io.github.finalwave.model.user.Gender;
import io.github.finalwave.model.user.User;
import io.github.finalwave.network.auth.RegisterFailReason;
import io.github.finalwave.network.auth.RegisterFailPayload;
import io.github.finalwave.network.auth.RegisterOkPayload;
import io.github.finalwave.network.auth.RegisterRequest;
import io.github.finalwave.server.db.ServerDatabase;
import io.github.finalwave.util.HashUtil;
import io.github.finalwave.util.RegisterRequestValidator;

public final class RegisterService {
    private final ServerDatabase database;

    public RegisterService(ServerDatabase database) {
        this.database = database;
    }

    public RegisterResult register(RegisterRequest request) {
        String validationFailure = RegisterRequestValidator.validate(request);
        if (validationFailure != null) {
            return RegisterResult.failure(validationFailure);
        }
        String username = request.getUsername().trim();
        String email = request.getEmail().trim();
        if (database.isUsernameTaken(username)) {
            return RegisterResult.failure(RegisterFailReason.USERNAME_TAKEN);
        }
        if (database.emailExists(email)) {
            return RegisterResult.failure(RegisterFailReason.EMAIL_TAKEN);
        }
        Gender gender = Gender.fromString(request.getGender().trim());
        if (gender == null) {
            return RegisterResult.failure(RegisterFailReason.INVALID_GENDER);
        }
        User user = new User(
                username,
                HashUtil.hashSHA256(request.getPassword()),
                request.getNickname().trim(),
                email,
                gender
        );
        Integer questionNumber = request.getSecurityQuestionNumber();
        if (questionNumber != null) {
            user.setSecurityQuestionId(questionNumber);
            user.setSecurityAnswerHash(HashUtil.hashSHA256(request.getSecurityAnswer()));
        }
        try {
            database.registerUser(user);
        } catch (RuntimeException exception) {
            return RegisterResult.failure(RegisterFailReason.SERVER_ERROR);
        }
        User loaded = database.getUser(username);
        if (loaded == null) {
            return RegisterResult.failure(RegisterFailReason.SERVER_ERROR);
        }
        return RegisterResult.success(toPayload(loaded));
    }

    private static RegisterOkPayload toPayload(User user) {
        return new RegisterOkPayload(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getEmail(),
                user.getGender().name(),
                user.getCoins(),
                user.getDiamonds(),
                user.getPlantFood()
        );
    }

    public static final class RegisterResult {
        private final RegisterOkPayload successPayload;
        private final RegisterFailPayload failurePayload;

        private RegisterResult(RegisterOkPayload successPayload, RegisterFailPayload failurePayload) {
            this.successPayload = successPayload;
            this.failurePayload = failurePayload;
        }

        public static RegisterResult success(RegisterOkPayload payload) {
            return new RegisterResult(payload, null);
        }

        public static RegisterResult failure(String reason) {
            return new RegisterResult(null, new RegisterFailPayload(reason));
        }

        public boolean isSuccess() {
            return successPayload != null;
        }

        public RegisterOkPayload successPayload() {
            return successPayload;
        }

        public RegisterFailPayload failurePayload() {
            return failurePayload;
        }
    }
}
