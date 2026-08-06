package io.github.finalwave.view.api;

import io.github.finalwave.model.scoregame.MeowPointBreakdown;

public interface ScoreGameView extends View {
    void showScoreGameMenu(int bestMeowPoint);

    void showCurrentMenu();

    void showMatchResult(MeowPointBreakdown breakdown, int bestMeowPoint, boolean newBest);

    void errorInvalidCommand();
}
