package io.github.finalwave.network.leaderboard;

import java.util.ArrayList;
import java.util.List;

public final class GetLeaderboardOkPayload {
    private List<LeaderboardRow> entries = new ArrayList<>();

    public GetLeaderboardOkPayload() {
    }

    public List<LeaderboardRow> getEntries() {
        return entries;
    }

    public void setEntries(List<LeaderboardRow> entries) {
        this.entries = entries == null ? new ArrayList<>() : entries;
    }
}
