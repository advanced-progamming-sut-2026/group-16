package controller;

import view.api.LeaderboardView;

public class LeaderboardController extends ViewController {
    @Override
    public void displayMenu() {
        // TODO: implement after Leaderboard is done.
    }

    @Override
    public void handleCommand(String input) {
        // TODO: implement after LeaderboardMenuCommands is defined.
        getLeaderboardView().errorInvalidCommand();
    }

    private LeaderboardView getLeaderboardView() {
        return (LeaderboardView) view;
    }
}
