package view.cli.minigame;

import view.api.minigame.StubMiniGameView;
import view.cli.CliView;

public class StubMiniGameViewCli extends CliView implements StubMiniGameView {

    @Override
    public void showComingSoon(String miniGameName) {
        displayMessage(miniGameName + " is coming soon.");
    }

    @Override
    public void errorInvalidCommand() {
        displayError("Invalid command. Use: menu exit");
    }
}
