package io.github.finalwave.view.api;

import io.github.finalwave.model.scoregame.MeowPointBreakdown;

public interface ScoreGameView extends View {
    void showScoreGameMenu(Integer bestMeowPoint);

    void showCurrentMenu();

    void showMatchResult(MeowPointBreakdown breakdown, Integer bestMeowPoint, boolean newBest);

    void errorInvalidCommand();

    void errorSubmitFailed(String reason);
}
