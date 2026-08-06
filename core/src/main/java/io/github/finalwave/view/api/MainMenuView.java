package io.github.finalwave.view.api;

public interface MainMenuView extends View {
    void showMainMenu(String nickname, boolean hasUnreadNews);

    void showCurrentMenu();

    void showLoggedOut();

    void errorInvalidMainMenuCommand();

    void errorInvalidMenuName();
}
