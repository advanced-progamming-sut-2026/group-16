package io.github.finalwave.network.auth;

public final class PasswordChangeFailPayload {
    private String reason;

    public PasswordChangeFailPayload() {
    }

    public PasswordChangeFailPayload(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
