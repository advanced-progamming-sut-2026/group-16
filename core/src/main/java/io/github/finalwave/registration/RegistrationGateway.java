package io.github.finalwave.registration;

import io.github.finalwave.network.auth.RegisterFailPayload;
import io.github.finalwave.network.auth.RegisterOkPayload;
import io.github.finalwave.network.auth.RegisterRequest;

public interface RegistrationGateway {
    interface Callback {
        void onSuccess(RegisterOkPayload payload);

        void onFailure(RegisterFailPayload payload);
    }

    void register(RegisterRequest request, Callback callback);
}
