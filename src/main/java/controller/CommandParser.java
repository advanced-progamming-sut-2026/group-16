package controller;

import model.App;
import model.user.User;
import model.user.UserDatabase;
import util.StayLoggedInStorage;
import view.cli.AuthViewCli;
import view.cli.MainMenuViewCli;
import view.cli.ProfileViewCli;

import java.util.Scanner;

public class CommandParser {
    private final AuthViewCli authView;
    private final MainMenuViewCli mainMenuView;
    private final ProfileViewCli profileView;
    private final RegistrationController registrationController;
    private final MainMenuController mainMenuController;
    private ViewController currentController;

    public CommandParser() {
        UserDatabase userDatabase = UserDatabase.getInstance();
        authView = new AuthViewCli();
        mainMenuView = new MainMenuViewCli();
        profileView = new ProfileViewCli();

        LoginController loginController = new LoginController(userDatabase);
        registrationController = new RegistrationController(userDatabase);

        ProfileController profileController = new ProfileController();
        mainMenuController = new MainMenuController(App.getInstance().getCurrentUser(), registrationController);

        loginController.setRegistrationController(registrationController);
        registrationController.setLoginController(loginController);
        profileController.setMainMenuController(mainMenuController);

        loginController.setView(authView);
        registrationController.setView(authView);
        profileController.setView(profileView);

        User stayLoggedInUser = restoreStayLoggedInUser(userDatabase);
        if (stayLoggedInUser != null) {
            switchController(new MainMenuController(stayLoggedInUser, registrationController));
        } else {
            switchController(registrationController);
        }
    }

    private User restoreStayLoggedInUser(UserDatabase db) {
        StayLoggedInStorage.Session session = StayLoggedInStorage.loadSession();
        if (session == null) {
            return null;
        }

        User user = db.getUser(session.username());
        if (user == null || !session.passwordHash().equals(user.getPasswordHash())) {
            StayLoggedInStorage.clear();
            return null;
        }
        return user;
    }

    public void run() {
        try (Scanner scanner = new Scanner(System.in)) {
            while (scanner.hasNextLine()) {
                parseAndExecute(scanner.nextLine());
            }
        }
    }

    public void parseAndExecute(String input) {
        if (input == null || currentController == null) {
            return;
        }

        String trimmed = input.trim();
        if (trimmed.isEmpty()) {
            return;
        }
        currentController.handleCommand(trimmed);
    }

    public void switchController(ViewController newController) {
        if (newController == null) {
            return;
        }

        this.currentController = newController;
        newController.setParser(this);
        if (newController instanceof MainMenuController) {
            newController.setView(mainMenuView);
        } else if (newController.getView() == null) {
            newController.setView(authView);
        }
        newController.displayMenu();
    }
}
