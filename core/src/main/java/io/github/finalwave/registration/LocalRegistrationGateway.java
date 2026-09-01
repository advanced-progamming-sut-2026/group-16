package io.github.finalwave.registration;

import io.github.finalwave.model.user.Gender;
import io.github.finalwave.model.user.User;
import io.github.finalwave.model.user.UserDatabase;
import io.github.finalwave.network.auth.RegisterFailPayload;
import io.github.finalwave.network.auth.RegisterFailReason;
import io.github.finalwave.network.auth.RegisterOkPayload;
import io.github.finalwave.network.auth.RegisterRequest;
import io.github.finalwave.util.HashUtil;
import io.github.finalwave.util.RegisterRequestValidator;

public final class LocalRegistrationGateway implements RegistrationGateway {
    private final UserDatabase database;

    public LocalRegistrationGateway(UserDatabase database) {
        this.database = database;
    }

    @Override
    public void register(RegisterRequest request, Callback callback) {
        String validationFailure = RegisterRequestValidator.validate(request);
        if (validationFailure != null) {
            callback.onFailure(new RegisterFailPayload(validationFailure));
            return;
        }
        String username = request.getUsername().trim();
        String email = request.getEmail().trim();
        if (database.isUsernameTaken(username)) {
            callback.onFailure(new RegisterFailPayload(RegisterFailReason.USERNAME_TAKEN));
            return;
        }
        if (database.emailExists(email)) {
            callback.onFailure(new RegisterFailPayload(RegisterFailReason.EMAIL_TAKEN));
            return;
        }
        Gender gender = Gender.fromString(request.getGender().trim());
        if (gender == null) {
            callback.onFailure(new RegisterFailPayload(RegisterFailReason.INVALID_GENDER));
            return;
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
            callback.onFailure(new RegisterFailPayload(RegisterFailReason.SERVER_ERROR));
            return;
        }
        User loaded = database.getUser(username);
        if (loaded == null) {
            callback.onFailure(new RegisterFailPayload(RegisterFailReason.SERVER_ERROR));
            return;
        }
        callback.onSuccess(toPayload(loaded));
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
                user.getPlantFood(),
                user.getSecurityQuestionId()
        );
    }
}
