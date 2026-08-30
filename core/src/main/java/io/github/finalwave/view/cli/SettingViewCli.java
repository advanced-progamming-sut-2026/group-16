package io.github.finalwave.view.cli;

import io.github.finalwave.view.api.SettingView;

public class SettingViewCli extends CliView implements SettingView {
    @Override
    public void showChangedDifficulty(int difficulty) {
        displayMessage("The difficulty level change to " + difficulty + ".");
    }

    @Override
    public void showSettingsMenu(int difficulty) {
        displayMessage("You are in the settings menu. Current difficulty: " + difficulty);
    }

    @Override
    public void showCurrentMenu(int difficulty) {
        displayMessage("Current menu: settings");
        displayMessage("Difficulty level: " + difficulty);
    }

    @Override
    public void showOnlineStatus(String username, String status) {
        displayMessage(username + ": " + status);
    }

    @Override
    public void errorInvalidCommand() {
        displayError("Invalid settings command.");
    }

    @Override
    public void errorInvalidDifficultyFormat() {
        displayError("Difficulty level must be a number between 1 and 5.");
    }

    @Override
    public void errorDifficultyOutOfRange() {
        displayError("Difficulty level must be between 1 and 5.");
    }
}
