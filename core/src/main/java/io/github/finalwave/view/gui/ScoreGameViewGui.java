package io.github.finalwave.view.gui;

import io.github.finalwave.controller.ScoreGameController;
import io.github.finalwave.model.scoregame.MeowPointBreakdown;
import io.github.finalwave.view.api.ScoreGameView;
import io.github.finalwave.view.gui.screen.ScreenRouter;

public final class ScoreGameViewGui extends GuiViewBase implements ScoreGameView {
    public ScoreGameViewGui(ScreenRouter router) {
        super(router);
    }

    public void bindController(ScoreGameController controller) {
    }

    @Override
    public void showScoreGameMenu(int bestMeowPoint) {
        router.refreshScoreGame();
    }

    @Override
    public void showCurrentMenu() {
        router.refreshScoreGame();
    }

    @Override
    public void showMatchResult(MeowPointBreakdown breakdown, int bestMeowPoint, boolean newBest) {
        router.showScoreGameResult(breakdown, bestMeowPoint, newBest);
    }

    @Override
    public void errorInvalidCommand() {
        toastError("Invalid score game command.");
    }
}
