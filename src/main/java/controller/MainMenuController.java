package controller;

import model.command.MainMenuCommands;
import model.user.User;
import model.user.UserDatabase;
import util.StayLoggedInStorage;
import view.api.MainMenuView;

import java.util.Locale;
import java.util.regex.Matcher;

public class MainMenuController extends ViewController {
    private final User activeUser;
    private final RegistrationController registrationController;
    private final UserDatabase userDatabase;

    public MainMenuController(User activeUser, RegistrationController registrationController, UserDatabase userDatabase) {
        this.activeUser = activeUser;
        this.registrationController = registrationController;
        this.userDatabase = userDatabase;
    }

    @Override
    public void displayMenu() {
        getMainMenuView().showMainMenu(activeUser.getNickname());
    }

    @Override
    public void handleCommand(String input) {
        for (MainMenuCommands cmd : MainMenuCommands.values()) {
            Matcher matcher = cmd.getMatcher(input);
            if (matcher == null) {
                continue;
            }
            switch (cmd) {
                case MENU_ENTER -> handleMenuEnter(matcher.group("menuName"));
                case MENU_SHOW_CURRENT -> handleShowCurrent();
                case LOGOUT -> handleLogout();
            }
            return;
        }
        getMainMenuView().errorInvalidMainMenuCommand();
    }

    private void handleMenuEnter(String menuName) {
        switch (normalizeMenuName(menuName)) {
            case "game" -> parser.switchController(new GameController(activeUser, userDatabase, this));
            case "settings" -> parser.switchController(new SettingController());
            case "news" -> parser.switchController(new NewsController());
            case "profile" -> {
                ProfileController profileController = new ProfileController();
                profileController.setMainMenuController(this);
                parser.switchController(profileController);
            }
            default -> getMainMenuView().errorInvalidMenuName();
        }
    }

    private void handleShowCurrent() {
        getMainMenuView().showCurrentMenu();
    }

    private void handleLogout() {
        StayLoggedInStorage.clear();
        getMainMenuView().showLoggedOut();
        parser.switchController(registrationController);
    }

    private String normalizeMenuName(String menuName) {
        return menuName.trim().toLowerCase(Locale.ROOT);
    }

    private MainMenuView getMainMenuView() {
        return (MainMenuView) view;
    }
}
