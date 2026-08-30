package io.github.finalwave.network.match;

public final class ChallengeInvitePayload {
    private String inviteId;
    private String fromUsername;

    public ChallengeInvitePayload() {
    }

    public ChallengeInvitePayload(String inviteId, String fromUsername) {
        this.inviteId = inviteId;
        this.fromUsername = fromUsername;
    }

    public String getInviteId() {
        return inviteId;
    }

    public void setInviteId(String inviteId) {
        this.inviteId = inviteId;
    }

    public String getFromUsername() {
        return fromUsername;
    }

    public void setFromUsername(String fromUsername) {
        this.fromUsername = fromUsername;
    }
}
