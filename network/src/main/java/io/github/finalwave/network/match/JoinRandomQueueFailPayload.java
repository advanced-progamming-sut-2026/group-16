package io.github.finalwave.network.match;

public final class JoinRandomQueueFailPayload {
    private ChallengeFailReason reason;

    public JoinRandomQueueFailPayload() {
    }

    public JoinRandomQueueFailPayload(ChallengeFailReason reason) {
        this.reason = reason;
    }

    public ChallengeFailReason getReason() {
        return reason;
    }

    public void setReason(ChallengeFailReason reason) {
        this.reason = reason;
    }
}
