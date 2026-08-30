package io.github.finalwave.network.score;

public final class SubmitScoreFailPayload {
    private String reason;

    public SubmitScoreFailPayload() {
    }

    public SubmitScoreFailPayload(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
