package io.github.finalwave.network.match;

public final class CheckUserStatusRequest {
    private String username;

    public CheckUserStatusRequest() {
    }

    public CheckUserStatusRequest(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
