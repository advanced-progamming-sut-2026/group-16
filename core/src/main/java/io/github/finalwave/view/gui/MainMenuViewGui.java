package io.github.finalwave.view.gui;

import io.github.finalwave.controller.MainMenuController;
import io.github.finalwave.view.api.MainMenuView;
import io.github.finalwave.view.gui.screen.ScreenRouter;


public final class MainMenuViewGui extends GuiViewBase implements MainMenuView {
    public MainMenuViewGui(ScreenRouter router) {
        super(router);
    }

    public void bindController(MainMenuController controller) {

    }

    @Override
    public void showMainMenu(String nickname, boolean hasUnreadNews) {
        router.updateMainMenuHeader(nickname, hasUnreadNews);
    }

    @Override
    public void showCurrentMenu() {
        toast("Current menu: main");
    }

    @Override
    public void showLoggedOut() {
        toast("You have been logged out.");
    }

    @Override
    public void errorInvalidMainMenuCommand() {
        toastError("Invalid main menu command.");
    }

    @Override
    public void errorInvalidMenuName() {
        toastError("Invalid menu name.");
    }
}
