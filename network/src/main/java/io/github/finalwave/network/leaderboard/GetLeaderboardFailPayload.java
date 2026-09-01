package io.github.finalwave.network.leaderboard;

public final class GetLeaderboardFailPayload {
    private String reason;

    public GetLeaderboardFailPayload() {
    }

    public GetLeaderboardFailPayload(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
