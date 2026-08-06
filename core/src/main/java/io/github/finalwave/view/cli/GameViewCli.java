package io.github.finalwave.view.cli;

import io.github.finalwave.view.api.GameView;

public class GameViewCli extends CliView implements GameView {
    @Override
    public void showGameMenu() {
        displayMessage("Game Menu");
    }

    @Override
    public void showCurrentMenu() {
        displayMessage("You are in the game menu.");
    }

    @Override
    public void showCoinWallet(int coins) {
        displayMessage("Coins: " + coins);
    }

    @Override
    public void showGemWallet(int diamonds) {
        displayMessage("Diamonds: " + diamonds);
    }

    @Override
    public void showCheatAdded(String type, int amount) {
        displayMessage("Added " + amount + " " + type + "(s).");
    }

    @Override
    public void errorInvalidCommand() {
        displayError("Invalid game menu command.");
    }

    @Override
    public void errorNotImplemented(String feature) {
        displayError("Only greenhouse path is implemented in this phase.");
    }

    @Override
    public void errorUnknownChapter(String chapterName) {
        displayError("Unknown chapter: " + chapterName);
    }

    @Override
    public void errorChapterLocked(String chapterName) {
        displayError("Chapter " + chapterName + " is locked.");
    }

    @Override
    public void errorLeaderboardNotImplemented() {
        displayError("Leaderboard is not implemented yet.");
    }

    @Override
    public void errorInvalidCheatAmount() {
        displayError("Invalid cheat amount.");
    }

    @Override
    public void errorInvalidCheatType() {
        displayError("Invalid cheat type.");
    }
}
