package io.github.finalwave.leaderboard;

import io.github.finalwave.model.leaderboard.LeaderboardEntry;

import java.util.List;

public interface LeaderboardGateway {
    void fetch(Callback callback);

    interface Callback {
        void onSuccess(List<LeaderboardEntry> entries);

        void onFailure(String reason);
    }
}
