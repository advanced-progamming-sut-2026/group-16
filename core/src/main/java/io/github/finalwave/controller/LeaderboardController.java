package io.github.finalwave.controller;

import io.github.finalwave.leaderboard.LeaderboardGateway;
import io.github.finalwave.model.command.LeaderboardMenuCommands;
import io.github.finalwave.model.leaderboard.LeaderboardEntry;
import io.github.finalwave.model.leaderboard.LeaderboardService;
import io.github.finalwave.model.leaderboard.LeaderboardSortColumn;
import io.github.finalwave.view.api.LeaderboardView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;

public class LeaderboardController extends ViewController {
    private final LeaderboardGateway leaderboardGateway;
    private LeaderboardSortColumn sortColumn = LeaderboardSortColumn.USERNAME;
    private boolean ascending = true;
    private List<LeaderboardEntry> entries = new ArrayList<>();

    public LeaderboardController(LeaderboardGateway leaderboardGateway) {
        this.leaderboardGateway = leaderboardGateway;
    }

    @Override
    public void displayMenu() {
        getLeaderboardView().showLeaderboardMenu();
        refreshEntries();
    }

    @Override
    public void handleCommand(String input) {
        for (LeaderboardMenuCommands cmd : LeaderboardMenuCommands.values()) {
            Matcher matcher = cmd.getMatcher(input);
            if (matcher == null) {
                continue;
            }
            switch (cmd) {
                case MENU_SHOW_CURRENT -> getLeaderboardView().showCurrentMenu();
                case MENU_EXIT -> back();
                case SORT -> sort(matcher.group("column"), matcher.group("order"));
                case REFRESH -> refresh();
            }
            return;
        }
        getLeaderboardView().errorInvalidCommand();
    }

    public void sort(String columnRaw) {
        sort(columnRaw, null);
    }

    public void sort(String columnRaw, String orderRaw) {
        handleSort(columnRaw, orderRaw);
    }

    public void refresh() {
        handleRefresh();
    }

    public void back() {
        navigator.pop();
    }

    private void handleSort(String columnRaw, String orderRaw) {
        LeaderboardSortColumn column = LeaderboardSortColumn.fromKey(columnRaw);
        if (column == null) {
            getLeaderboardView().errorInvalidSortColumn();
            return;
        }

        if (orderRaw == null || orderRaw.isBlank()) {
            if (column == sortColumn) {
                ascending = !ascending;
            } else {
                sortColumn = column;
                ascending = true;
            }
        } else {
            String order = orderRaw.trim().toLowerCase(Locale.ROOT);
            if (!"asc".equals(order) && !"desc".equals(order)) {
                getLeaderboardView().errorInvalidSortOrder();
                return;
            }
            sortColumn = column;
            ascending = "asc".equals(order);
        }
        showTable();
    }

    private void handleRefresh() {
        refreshEntries();
    }

    private void refreshEntries() {
        entries = new ArrayList<>();
        showTable();
        leaderboardGateway.fetch(new LeaderboardGateway.Callback() {
            @Override
            public void onSuccess(List<LeaderboardEntry> loaded) {
                entries = loaded == null ? new ArrayList<>() : new ArrayList<>(loaded);
                showTable();
            }

            @Override
            public void onFailure(String reason) {
                entries = new ArrayList<>();
                showTable();
                getLeaderboardView().errorLoadFailed(reason);
            }
        });
    }

    private void showTable() {
        List<LeaderboardEntry> sorted =
                LeaderboardService.sort(entries, sortColumn, ascending);
        getLeaderboardView().showLeaderboard(sorted, sortColumn, ascending);
    }

    private LeaderboardView getLeaderboardView() {
        return (LeaderboardView) view;
    }
}
