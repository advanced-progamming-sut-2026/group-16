package io.github.finalwave.network.match;

public final class CheckUserStatusResponse {
    private UserStatus status;

    public CheckUserStatusResponse() {
    }

    public CheckUserStatusResponse(UserStatus status) {
        this.status = status;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }
}
