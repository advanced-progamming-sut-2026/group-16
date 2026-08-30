package io.github.finalwave.network.auth;

public final class LoginFailPayload {
    private String reason;

    public LoginFailPayload() {
    }

    public LoginFailPayload(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
