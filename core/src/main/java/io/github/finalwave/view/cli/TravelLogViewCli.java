package io.github.finalwave.view.cli;

import io.github.finalwave.view.api.TravelLogView;

import java.util.List;

public class TravelLogViewCli extends CliView implements TravelLogView {
    @Override
    public void showCurrentMenu() {
        displayMessage("You are in the Travel Log menu.");
    }

    @Override
    public void showTravelLogPage(String pageName, List<String> questLines) {
        displayMessage("Travel Log - " + pageName + " quests:");
        if (questLines == null || questLines.isEmpty()) {
            displayMessage("(none)");
            return;
        }
        for (String line : questLines) {
            displayMessage(line);
        }
    }

    @Override
    public void showProgressSummary(List<String> lines) {
        displayMessage("Travel Log - Progress summary:");
        if (lines == null || lines.isEmpty()) {
            displayMessage("(none)");
            return;
        }
        for (String line : lines) {
            displayMessage(line);
        }
    }

    @Override
    public void errorInvalidCommand() {
        displayError("Invalid travel log command.");
    }

    @Override
    public void errorPageNameRequired() {
        displayError("Page name is required.");
    }

    @Override
    public void errorUnknownPage(String pageName) {
        displayError("Unknown travel log page: " + pageName
                + ". Use daily, main, epic, progress, or minigames.");
    }
}
