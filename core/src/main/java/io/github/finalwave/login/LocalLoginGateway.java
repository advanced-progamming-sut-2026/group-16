package io.github.finalwave.login;

import io.github.finalwave.model.user.User;
import io.github.finalwave.model.user.UserDatabase;
import io.github.finalwave.network.auth.LoginFailPayload;
import io.github.finalwave.network.auth.LoginFailReason;
import io.github.finalwave.network.auth.LoginOkPayload;
import io.github.finalwave.network.auth.LoginRequest;
import io.github.finalwave.profile.ProfileExporter;

public final class LocalLoginGateway implements LoginGateway {
    private final UserDatabase database;

    public LocalLoginGateway(UserDatabase database) {
        this.database = database;
    }

    @Override
    public void login(LoginRequest request, Callback callback) {
        if (request == null
                || request.getUsername() == null
                || request.getUsername().isBlank()
                || request.getPassword() == null
                || request.getPassword().isBlank()) {
            callback.onFailure(new LoginFailPayload(LoginFailReason.INVALID_INPUT));
            return;
        }
        String username = request.getUsername().trim();
        User user = database.getUser(username);
        if (user == null || !user.authenticate(request.getPassword())) {
            callback.onFailure(new LoginFailPayload(LoginFailReason.BAD_CREDENTIALS));
            return;
        }
        try {
            LoginOkPayload payload = ProfileExporter.export(user, database);
            callback.onSuccess(payload);
        } catch (RuntimeException exception) {
            callback.onFailure(new LoginFailPayload(LoginFailReason.SERVER_ERROR));
        }
    }

    @Override
    public void logout() {
    }
}
