package io.github.finalwave.login;

import io.github.finalwave.network.auth.ChangePasswordRequest;
import io.github.finalwave.network.auth.PasswordChangeFailPayload;
import io.github.finalwave.network.auth.PasswordChangeOkPayload;
import io.github.finalwave.network.auth.ResetPasswordRequest;
import io.github.finalwave.network.auth.SecurityQuestionLookupOkPayload;
import io.github.finalwave.network.auth.SecurityQuestionLookupRequest;

public interface PasswordChangeGateway {
    void resetPassword(ResetPasswordRequest request, Callback callback);

    void changePassword(ChangePasswordRequest request, Callback callback);

    default void lookupSecurityQuestion(SecurityQuestionLookupRequest request, LookupCallback callback) {
        callback.onFailure(new PasswordChangeFailPayload(
                io.github.finalwave.network.auth.PasswordChangeFailReason.NOT_CONNECTED));
    }

    interface Callback {
        void onSuccess(PasswordChangeOkPayload payload);

        void onFailure(PasswordChangeFailPayload payload);
    }

    interface LookupCallback {
        void onSuccess(SecurityQuestionLookupOkPayload payload);

        void onFailure(PasswordChangeFailPayload payload);
    }
}
