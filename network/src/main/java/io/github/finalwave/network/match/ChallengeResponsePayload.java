package io.github.finalwave.network.match;

public final class ChallengeResponsePayload {
    private String inviteId;
    private boolean accepted;

    public ChallengeResponsePayload() {
    }

    public ChallengeResponsePayload(String inviteId, boolean accepted) {
        this.inviteId = inviteId;
        this.accepted = accepted;
    }

    public String getInviteId() {
        return inviteId;
    }

    public void setInviteId(String inviteId) {
        this.inviteId = inviteId;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }
}
