package io.github.finalwave.network.sync;

public final class SyncFailPayload {
    private String reason;

    public SyncFailPayload() {
    }

    public SyncFailPayload(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
