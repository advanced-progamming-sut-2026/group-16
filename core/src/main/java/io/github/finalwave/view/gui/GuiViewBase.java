package io.github.finalwave.view.gui;

import io.github.finalwave.view.api.View;
import io.github.finalwave.view.gui.screen.MenuScreen;
import io.github.finalwave.view.gui.screen.ScreenRouter;


public abstract class GuiViewBase implements View {
    protected final ScreenRouter router;

    protected GuiViewBase(ScreenRouter router) {
        this.router = router;
    }

    @Override
    public void displayMessage(String line) {
        MenuScreen screen = router.currentMenuScreen();
        if (screen != null) {
            screen.toastMessage(line);
        }
    }

    @Override
    public void displayError(String line) {
        MenuScreen screen = router.currentMenuScreen();
        if (screen != null) {
            screen.toastError(line);
        }
    }

    protected void toast(String message) {
        displayMessage(message);
    }

    protected void toastError(String message) {
        displayError(message);
    }
}
