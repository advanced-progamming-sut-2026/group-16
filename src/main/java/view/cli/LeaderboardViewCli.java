package view.cli;

import view.api.LeaderboardView;

public class LeaderboardViewCli extends CliView implements LeaderboardView {
    @Override
    public void showLeaderboard() {
        // TODO: implement after the Leaderboard is done.
    }

    @Override
    public void errorInvalidCommand() {
        displayError("Invalid leaderboard command.");
    }
}
