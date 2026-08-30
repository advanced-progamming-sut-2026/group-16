package io.github.finalwave.view.gui;

import io.github.finalwave.controller.LeaderboardController;
import io.github.finalwave.model.leaderboard.LeaderboardEntry;
import io.github.finalwave.model.leaderboard.LeaderboardSortColumn;
import io.github.finalwave.view.api.LeaderboardView;
import io.github.finalwave.view.gui.screen.ScreenRouter;

import java.util.List;

public final class LeaderboardViewGui extends GuiViewBase implements LeaderboardView {
    public LeaderboardViewGui(ScreenRouter router) {
        super(router);
    }

    public void bindController(LeaderboardController controller) {
    }

    @Override
    public void showLeaderboardMenu() {
    }

    @Override
    public void showLeaderboard(
            List<LeaderboardEntry> entries,
            LeaderboardSortColumn column,
            boolean ascending) {
        router.showLeaderboardTable(entries, column, ascending);
    }

    @Override
    public void showCurrentMenu() {
        toast("Current menu: leaderboard");
    }

    @Override
    public void errorInvalidCommand() {
        toastError("Invalid leaderboard command.");
    }

    @Override
    public void errorInvalidSortColumn() {
        toastError("Invalid sort column.");
    }

    @Override
    public void errorInvalidSortOrder() {
        toastError("Invalid sort order. Use asc or desc.");
    }

    @Override
    public void errorLoadFailed(String reason) {
        toastError("Could not load leaderboard: " + (reason == null ? "unknown error" : reason));
    }
}
