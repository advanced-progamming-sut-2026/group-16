package io.github.finalwave.network.match;

public final class ChallengeRejectedPayload {
    private String inviteId;

    public ChallengeRejectedPayload() {
    }

    public ChallengeRejectedPayload(String inviteId) {
        this.inviteId = inviteId;
    }

    public String getInviteId() {
        return inviteId;
    }

    public void setInviteId(String inviteId) {
        this.inviteId = inviteId;
    }
}
