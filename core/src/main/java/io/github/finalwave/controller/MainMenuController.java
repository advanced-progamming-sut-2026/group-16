package io.github.finalwave.controller;

import io.github.finalwave.model.command.MainMenuCommands;
import io.github.finalwave.model.user.User;
import io.github.finalwave.model.user.UserDatabase;
import io.github.finalwave.util.StayLoggedInStorage;
import io.github.finalwave.view.api.MainMenuView;

import java.util.Locale;
import java.util.regex.Matcher;

public class MainMenuController extends ViewController {
    private final User activeUser;
    private final UserDatabase userDatabase;

    public MainMenuController(User activeUser, UserDatabase userDatabase) {
        this.activeUser = activeUser;
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
            case "game" -> navigator.push(new GameController(activeUser, userDatabase));
            case "settings" -> navigator.push(new SettingController(activeUser, userDatabase));
            case "news" -> navigator.push(new NewsController(activeUser, userDatabase));
            case "profile" -> navigator.push(new ProfileController(userDatabase));
            case "leaderboard" -> navigator.push(new LeaderboardController(userDatabase));
            case "score-game", "scoregame" -> navigator.push(
                    new ScoreGameController(activeUser, userDatabase));
            default -> getMainMenuView().errorInvalidMenuName();
        }
    }

    private void handleShowCurrent() {
        getMainMenuView().showCurrentMenu();
    }

    private void handleLogout() {
        StayLoggedInStorage.clear();
        getMainMenuView().showLoggedOut();
        navigator.reset(new RegistrationController(userDatabase));
    }

    private String normalizeMenuName(String menuName) {
        return menuName.trim().toLowerCase(Locale.ROOT).replace(' ', '-');
    }

    private MainMenuView getMainMenuView() {
        return (MainMenuView) view;
    }
}
