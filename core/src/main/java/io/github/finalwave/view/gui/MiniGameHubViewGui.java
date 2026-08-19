package io.github.finalwave.view.gui;

import io.github.finalwave.controller.MiniGameHubController;
import io.github.finalwave.model.minigame.MiniGameId;
import io.github.finalwave.view.api.minigame.MiniGameHubView;
import io.github.finalwave.view.gui.screen.ScreenRouter;

import java.util.List;


public final class MiniGameHubViewGui extends GuiViewBase implements MiniGameHubView {
    public MiniGameHubViewGui(ScreenRouter router) {
        super(router);
    }

    public void bindController(MiniGameHubController controller) {
    }

    @Override
    public void showCurrentMenu() {
        router.refreshMiniGameHub();
    }

    @Override
    public void showGames(List<String> lines) {
        router.refreshMiniGameHub();
    }

    @Override
    public void showStages(MiniGameId id, List<String> lines) {
        router.refreshMiniGameHub();
    }

    @Override
    public void showEnteredGame(MiniGameId id) {
        router.refreshMiniGameHub();
    }

    @Override
    public void showComingSoon(MiniGameId id) {
        String name = id == null ? "This minigame" : id.getDisplayName();
        toast(name + " is coming soon.");
        router.refreshMiniGameHub();
    }

    @Override
    public void errorInvalidCommand() {
        toastError("Invalid mini-game command.");
    }

    @Override
    public void errorUnknownGame(String name) {
        toastError("Unknown mini-game: " + name + ".");
    }

    @Override
    public void errorGameLocked(String name) {
        toastError(name + " is locked.");
    }

    @Override
    public void errorNoGameSelected() {
        toastError("Select a mini-game first.");
    }

    @Override
    public void errorInvalidStage() {
        toastError("Invalid stage number.");
    }

    @Override
    public void errorStageLocked(int stage) {
        toastError("Stage " + stage + " is locked. Complete earlier stages first.");
    }

    @Override
    public void errorStageNotFound(int stage) {
        toastError("Stage not found: " + stage + ".");
    }
}
