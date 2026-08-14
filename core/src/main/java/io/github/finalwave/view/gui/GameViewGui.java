package io.github.finalwave.view.gui;

import io.github.finalwave.controller.GameController;
import io.github.finalwave.view.api.GameView;
import io.github.finalwave.view.gui.screen.ScreenRouter;

public final class GameViewGui extends GuiViewBase implements GameView {
    public GameViewGui(ScreenRouter router) {
        super(router);
    }

    public void bindController(GameController controller) {
    }

    @Override
    public void showGameMenu() {
        router.refreshChapterSelect();
    }

    @Override
    public void showCurrentMenu() {
        router.refreshChapterSelect();
    }

    @Override
    public void showCoinWallet(int coins) {
        toast("Coins: " + coins);
    }

    @Override
    public void showGemWallet(int diamonds) {
        toast("Diamonds: " + diamonds);
    }

    @Override
    public void showCheatAdded(String type, int amount) {
        toast("Added " + amount + " " + type + "(s).");
    }

    @Override
    public void errorInvalidCommand() {
        toastError("Invalid game menu command.");
    }

    @Override
    public void errorNotImplemented(String feature) {
        toastError("Not implemented: " + feature);
    }

    @Override
    public void errorUnknownChapter(String chapterName) {
        toastError("Unknown chapter: " + chapterName);
    }

    @Override
    public void errorChapterLocked(String chapterName) {
        toastError(chapterName + " is locked.");
    }

    @Override
    public void errorLeaderboardNotImplemented() {
        toastError("Leaderboard is not implemented yet.");
    }

    @Override
    public void errorInvalidCheatAmount() {
        toastError("Invalid cheat amount.");
    }

    @Override
    public void errorInvalidCheatType() {
        toastError("Invalid cheat type.");
    }
}
