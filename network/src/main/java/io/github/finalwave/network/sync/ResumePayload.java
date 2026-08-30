package io.github.finalwave.network.sync;

public final class ResumePayload {
    private String username;
    private String passwordHash;

    public ResumePayload() {
    }

    public ResumePayload(String username, String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
}
