package io.github.finalwave.network.match;

public final class JoinRandomQueueOkPayload {
    private boolean waiting;

    public JoinRandomQueueOkPayload() {
    }

    public JoinRandomQueueOkPayload(boolean waiting) {
        this.waiting = waiting;
    }

    public boolean isWaiting() {
        return waiting;
    }

    public void setWaiting(boolean waiting) {
        this.waiting = waiting;
    }
}
