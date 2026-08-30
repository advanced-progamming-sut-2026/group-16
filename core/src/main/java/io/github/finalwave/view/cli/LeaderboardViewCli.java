package io.github.finalwave.view.cli;

import io.github.finalwave.model.leaderboard.LeaderboardEntry;
import io.github.finalwave.model.leaderboard.LeaderboardSortColumn;
import io.github.finalwave.view.api.LeaderboardView;

import java.util.List;

public class LeaderboardViewCli extends CliView implements LeaderboardView {
    @Override
    public void showLeaderboardMenu() {
        displayMessage("Leaderboard");
        displayMessage("Commands: sort -c <column> [-o asc|desc] | refresh | menu exit");
        displayMessage("Columns: username | progress | minigames | daily | nondaily | score");
    }

    @Override
    public void showLeaderboard(
            List<LeaderboardEntry> entries,
            LeaderboardSortColumn column,
            boolean ascending) {
        String order = ascending ? "asc" : "desc";
        String columnKey = column == null ? "username" : column.getKey();
        displayMessage("Sorted by " + columnKey + " (" + order + ")");
        displayMessage(String.format(
                "%-16s %-22s %10s %8s %10s %10s",
                "Username",
                "Progress",
                "Minigames",
                "Daily",
                "NonDaily",
                "BestScore"));
        if (entries == null || entries.isEmpty()) {
            displayMessage("(no players)");
            return;
        }
        for (LeaderboardEntry entry : entries) {
            displayMessage(String.format(
                    "%-16s %-22s %10d %8d %10d %10s",
                    entry.username(),
                    entry.progressLabel(),
                    entry.minigameCount(),
                    entry.dailyQuestCount(),
                    entry.nonDailyQuestCount(),
                    formatScore(entry.bestScore())));
        }
    }

    private static String formatScore(Integer score) {
        return score == null ? "-" : String.valueOf(score);
    }

    @Override
    public void showCurrentMenu() {
        displayMessage("Current menu: leaderboard");
    }

    @Override
    public void errorInvalidCommand() {
        displayError("Invalid leaderboard command.");
    }

    @Override
    public void errorInvalidSortColumn() {
        displayError("Invalid sort column.");
    }

    @Override
    public void errorInvalidSortOrder() {
        displayError("Invalid sort order. Use asc or desc.");
    }

    @Override
    public void errorLoadFailed(String reason) {
        displayError("Could not load leaderboard: " + (reason == null ? "unknown error" : reason));
    }
}
