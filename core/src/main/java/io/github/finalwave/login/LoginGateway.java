package io.github.finalwave.login;

import io.github.finalwave.network.auth.LoginFailPayload;
import io.github.finalwave.network.auth.LoginOkPayload;
import io.github.finalwave.network.auth.LoginRequest;

public interface LoginGateway {
    interface Callback {
        void onSuccess(LoginOkPayload payload);

        void onFailure(LoginFailPayload payload);
    }

    void login(LoginRequest request, Callback callback);

    void logout();
}
