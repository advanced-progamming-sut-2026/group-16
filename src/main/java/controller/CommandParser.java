package controller;

import model.user.UserDatabase;
import view.cli.AuthViewCli;
import view.cli.MainMenuViewCli;

import java.util.Scanner;

public class CommandParser {
    private final AuthViewCli authView;
    private final MainMenuViewCli mainMenuView;
    private final RegistrationController registrationController;
    private ViewController currentController;

    public CommandParser() {
        UserDatabase userDatabase = UserDatabase.getInstance();
        authView = new AuthViewCli();
        mainMenuView = new MainMenuViewCli();

        LoginController loginController = new LoginController(userDatabase);
        registrationController = new RegistrationController(userDatabase);

        loginController.setRegistrationController(registrationController);
        registrationController.setLoginController(loginController);

        loginController.setView(authView);
        registrationController.setView(authView);

        switchController(registrationController);
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
