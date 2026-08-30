package io.github.finalwave.network.match;

public final class ChallengeFailPayload {
    private ChallengeFailReason reason;

    public ChallengeFailPayload() {
    }

    public ChallengeFailPayload(ChallengeFailReason reason) {
        this.reason = reason;
    }

    public ChallengeFailReason getReason() {
        return reason;
    }

    public void setReason(ChallengeFailReason reason) {
        this.reason = reason;
    }
}
