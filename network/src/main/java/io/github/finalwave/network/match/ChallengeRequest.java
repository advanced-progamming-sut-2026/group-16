package io.github.finalwave.network.match;

public final class ChallengeRequest {
    private String targetUsername;

    public ChallengeRequest() {
    }

    public ChallengeRequest(String targetUsername) {
        this.targetUsername = targetUsername;
    }

    public String getTargetUsername() {
        return targetUsername;
    }

    public void setTargetUsername(String targetUsername) {
        this.targetUsername = targetUsername;
    }
}
