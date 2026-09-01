package io.github.finalwave.leaderboard;

public final class FailingLeaderboardGateway implements LeaderboardGateway {
    private final String reason;

    public FailingLeaderboardGateway(String reason) {
        this.reason = reason;
    }

    @Override
    public void fetch(Callback callback) {
        if (callback != null) {
            callback.onFailure(reason);
        }
    }
}
