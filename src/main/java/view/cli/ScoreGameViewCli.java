package view.cli;

import view.api.ScoreGameView;

public class ScoreGameViewCli extends CliView implements ScoreGameView {
    @Override
    public void showScoreGameMenu(int bestMeioPoint) {
        displayMessage("Score game");
        displayMessage("Playable score game is not available yet.");
        displayMessage("Best meiopoint: " + bestMeioPoint);
        displayMessage("Commands: menu exit");
    }

    @Override
    public void showCurrentMenu() {
        displayMessage("Current menu: score-game");
    }

    @Override
    public void errorInvalidCommand() {
        displayError("Invalid score game command.");
    }
}
