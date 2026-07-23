package view.api;

import model.scoregame.MeowPointBreakdown;

public interface ScoreGameView extends View {
    void showScoreGameMenu(int bestMeowPoint);

    void showCurrentMenu();

    void showMatchResult(MeowPointBreakdown breakdown, int bestMeowPoint, boolean newBest);

    void errorInvalidCommand();
}
