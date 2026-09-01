package io.github.finalwave.controller;

import io.github.finalwave.model.command.MainMenuCommands;
import io.github.finalwave.model.user.User;
import io.github.finalwave.model.user.UserDatabase;
import io.github.finalwave.login.LoginGateway;
import io.github.finalwave.leaderboard.LeaderboardGateway;
import io.github.finalwave.score.ScoreSubmitGateway;
import io.github.finalwave.model.App;
import io.github.finalwave.registration.RegistrationGateway;
import io.github.finalwave.util.StayLoggedInStorage;
import io.github.finalwave.view.api.MainMenuView;

import java.util.Locale;
import java.util.regex.Matcher;

public class MainMenuController extends ViewController {
    public enum Destination {
        GAME,
        SETTINGS,
        NEWS,
        PROFILE,
        LEADERBOARD,
        SCORE_GAME,
        GREENHOUSE
    }

    private final User activeUser;
    private final UserDatabase userDatabase;
    private final RegistrationGateway registrationGateway;
    private final LoginGateway loginGateway;
    private final LeaderboardGateway leaderboardGateway;
    private final ScoreSubmitGateway scoreSubmitGateway;

    public MainMenuController(
            User activeUser,
            UserDatabase userDatabase,
            RegistrationGateway registrationGateway,
            LoginGateway loginGateway,
            LeaderboardGateway leaderboardGateway,
            ScoreSubmitGateway scoreSubmitGateway
    ) {
        this.activeUser = activeUser;
        this.userDatabase = userDatabase;
        this.registrationGateway = registrationGateway;
        this.loginGateway = loginGateway;
        this.leaderboardGateway = leaderboardGateway;
        this.scoreSubmitGateway = scoreSubmitGateway;
    }

    public User getActiveUser() {
        return activeUser;
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
                case LOGOUT -> logout();
            }
            return;
        }
        getMainMenuView().errorInvalidMainMenuCommand();
    }

    public void open(Destination destination) {
        switch (destination) {
            case GAME -> navigator.push(new GameController(activeUser, userDatabase, leaderboardGateway));
            case SETTINGS -> navigator.push(new SettingController(activeUser, userDatabase));
            case NEWS -> navigator.push(new NewsController(activeUser, userDatabase));
            case PROFILE -> navigator.push(new ProfileController(userDatabase));
            case LEADERBOARD -> navigator.push(new LeaderboardController(leaderboardGateway));
            case SCORE_GAME -> navigator.push(new ScoreGameController(activeUser, userDatabase, scoreSubmitGateway));
            case GREENHOUSE -> navigator.push(new GreenhouseController(activeUser, userDatabase));
        }
    }

    public void logout() {
        loginGateway.logout();
        StayLoggedInStorage.clear();
        App.getInstance().setCurrentUser(null);
        getMainMenuView().showLoggedOut();
        navigator.reset(new RegistrationController(registrationGateway, userDatabase, loginGateway, leaderboardGateway, scoreSubmitGateway));
    }

    private void handleMenuEnter(String menuName) {
        switch (normalizeMenuName(menuName)) {
            case "game" -> open(Destination.GAME);
            case "settings" -> open(Destination.SETTINGS);
            case "news" -> open(Destination.NEWS);
            case "profile" -> open(Destination.PROFILE);
            case "leaderboard" -> open(Destination.LEADERBOARD);
            case "score-game", "scoregame" -> open(Destination.SCORE_GAME);
            default -> getMainMenuView().errorInvalidMenuName();
        }
    }

    private void handleShowCurrent() {
        getMainMenuView().showCurrentMenu();
    }

    private String normalizeMenuName(String menuName) {
        return menuName.trim().toLowerCase(Locale.ROOT).replace(' ', '-');
    }

    private MainMenuView getMainMenuView() {
        return (MainMenuView) view;
    }
}
