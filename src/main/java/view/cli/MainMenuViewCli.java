package view.cli;

import view.api.MainMenuView;

public class MainMenuViewCli extends CliView implements MainMenuView {
    @Override
    public void showMainMenu(String nickname) {
        displayMessage("Main menu");
        displayMessage("Welcome, " + nickname + "!");
    }

    @Override
    public void showCurrentMenu() {
        displayMessage("Current menu: main");
    }

    @Override
    public void showLoggedOut() {
        displayMessage("You have been logged out.");
    }

    @Override
    public void errorInvalidMainMenuCommand() {
        displayError("Invalid main menu command.");
    }

    @Override
    public void errorInvalidMenuName() {
        displayError("Invalid menu name.");
    }
}
