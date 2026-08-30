package io.github.finalwave.network.match;

public final class ChallengeTimeoutPayload {
    private String inviteId;

    public ChallengeTimeoutPayload() {
    }

    public ChallengeTimeoutPayload(String inviteId) {
        this.inviteId = inviteId;
    }

    public String getInviteId() {
        return inviteId;
    }

    public void setInviteId(String inviteId) {
        this.inviteId = inviteId;
    }
}
