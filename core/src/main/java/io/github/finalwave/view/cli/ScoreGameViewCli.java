package io.github.finalwave.view.cli;

import io.github.finalwave.model.scoregame.MeowPointBreakdown;
import io.github.finalwave.view.api.ScoreGameView;

public class ScoreGameViewCli extends CliView implements ScoreGameView {
    @Override
    public void showScoreGameMenu(Integer bestMeowPoint) {
        displayMessage("Score game");
        displayMessage("Best meowpoint: " + formatBest(bestMeowPoint));
        displayMessage("Commands: start | menu exit");
    }

    @Override
    public void showCurrentMenu() {
        displayMessage("Current menu: score-game");
    }

    @Override
    public void showMatchResult(MeowPointBreakdown breakdown, Integer bestMeowPoint, boolean newBest) {
        displayMessage("Match finished. Meowpoint: " + breakdown.total());
        for (var entry : breakdown.patternScores().entrySet()) {
            displayMessage("  " + entry.getKey() + ": " + entry.getValue());
        }
        displayMessage("Best meowpoint: " + formatBest(bestMeowPoint));
        if (newBest) {
            displayMessage("New best meowpoint!");
        }
    }

    @Override
    public void errorInvalidCommand() {
        displayError("Invalid score game command.");
    }

    @Override
    public void errorSubmitFailed(String reason) {
        displayError("Could not submit score: " + (reason == null ? "unknown error" : reason));
    }

    private static String formatBest(Integer bestMeowPoint) {
        return bestMeowPoint == null ? "-" : String.valueOf(bestMeowPoint);
    }
}
