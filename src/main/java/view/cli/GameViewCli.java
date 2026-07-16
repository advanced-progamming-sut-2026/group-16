package view.cli;

import view.api.GameView;

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
}
