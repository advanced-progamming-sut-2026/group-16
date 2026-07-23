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

    public
    MainMenuController(User activeUser, RegistrationController registrationController, UserDatabase userDatabase) {
        this.activeUser = activeUser;
        this.registrationController = registrationController;
        this.userDatabase = userDatabase;
    }

    @Override
    public void displayMenu() {
        getMainMenuView().showMainMenu(activeUser.getNickname(), activeUser.hasUnreadNews());
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
            case "settings" -> parser.switchController(
                    new SettingController(activeUser, userDatabase, this));
            case "news" -> parser.switchController(new NewsController(activeUser, userDatabase, this));
            case "profile" -> {
                ProfileController profileController = new ProfileController(userDatabase);
                profileController.setMainMenuController(this);
                parser.switchController(profileController);
            }
            case "leaderboard" -> parser.switchController(
                    new LeaderboardController(userDatabase, this));
            case "score-game", "scoregame" -> parser.switchController(
                    new ScoreGameController(activeUser, userDatabase, this));
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
        return menuName.trim().toLowerCase(Locale.ROOT).replace(' ', '-');
    }

    private MainMenuView getMainMenuView() {
        return (MainMenuView) view;
    }
}
