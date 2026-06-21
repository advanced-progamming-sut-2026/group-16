package view.api;

public interface MainMenuView extends View {
    void showMainMenu(String nickname);

    void showCurrentMenu();

    void showLoggedOut();

    void errorInvalidMainMenuCommand();

    void errorInvalidMenuName();
}
