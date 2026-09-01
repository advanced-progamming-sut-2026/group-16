package io.github.finalwave.network.auth;

public final class PasswordChangeOkPayload {
    private String username;

    public PasswordChangeOkPayload() {
    }

    public PasswordChangeOkPayload(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
