package view.api;

import model.leaderboard.LeaderboardEntry;
import model.leaderboard.LeaderboardSortColumn;

import java.util.List;

public interface LeaderboardView extends View {
    void showLeaderboardMenu();

    void showLeaderboard(
            List<LeaderboardEntry> entries,
            LeaderboardSortColumn column,
            boolean ascending);

    void showCurrentMenu();

    void errorInvalidCommand();

    void errorInvalidSortColumn();

    void errorInvalidSortOrder();
}
