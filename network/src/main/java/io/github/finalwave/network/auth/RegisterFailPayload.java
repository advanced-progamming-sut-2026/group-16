package io.github.finalwave.network.auth;

public final class RegisterFailPayload {
    private String reason;

    public RegisterFailPayload() {
    }

    public RegisterFailPayload(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
