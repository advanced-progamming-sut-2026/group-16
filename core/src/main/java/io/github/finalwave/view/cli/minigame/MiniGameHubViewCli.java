package io.github.finalwave.view.cli.minigame;

import io.github.finalwave.model.minigame.MiniGameId;
import io.github.finalwave.view.api.minigame.MiniGameHubView;
import io.github.finalwave.view.cli.CliView;

import java.util.List;

public class MiniGameHubViewCli extends CliView implements MiniGameHubView {

    @Override
    public void showCurrentMenu() {
        displayMessage("You are in the Mini-Games hub.");
    }

    @Override
    public void showGames(List<String> lines) {
        displayMessage("Available mini-games:");
        if (lines == null || lines.isEmpty()) {
            displayMessage("(none)");
            return;
        }
        for (String line : lines) {
            displayMessage(line);
        }
    }

    @Override
    public void showStages(MiniGameId id, List<String> lines) {
        displayMessage("Stages for " + id.getDisplayName() + ":");
        if (lines == null || lines.isEmpty()) {
            displayMessage("(none)");
            return;
        }
        for (String line : lines) {
            displayMessage(line);
        }
    }

    @Override
    public void showEnteredGame(MiniGameId id) {
        displayMessage("Selected mini-game: " + id.getDisplayName());
    }

    @Override
    public void showComingSoon(MiniGameId id) {
        displayMessage(id.getDisplayName() + " is coming soon.");
    }

    @Override
    public void errorInvalidCommand() {
        displayError("Invalid mini-game command.");
    }

    @Override
    public void errorUnknownGame(String name) {
        displayError("Unknown mini-game: " + name);
    }

    @Override
    public void errorGameLocked(String name) {
        displayError("Mini-game is locked: " + name);
    }

    @Override
    public void errorNoGameSelected() {
        displayError("Select a mini-game first with: enter game -n <name>");
    }

    @Override
    public void errorInvalidStage() {
        displayError("Invalid stage number.");
    }

    @Override
    public void errorStageLocked(int stage) {
        displayError("Stage " + stage + " is locked. Complete earlier stages first.");
    }

    @Override
    public void errorStageNotFound(int stage) {
        displayError("Stage not found: " + stage);
    }
}
