package controller;

import model.App;
import model.user.User;
import model.user.UserDatabase;
import util.StayLoggedInStorage;
import view.cli.AdventureViewCli;
import view.cli.AuthViewCli;
import view.cli.GamePlayViewCli;
import view.cli.GameViewCli;
import view.cli.GreenhouseViewCli;
import view.cli.MainMenuViewCli;
import view.cli.PlantSelectionViewCli;
import view.cli.ProfileViewCli;
import view.cli.ShopViewCli;
import view.cli.TravelLogViewCli;
import view.cli.SettingViewCli;

import java.util.Scanner;

public class CommandParser {
    private final AuthViewCli authView;
    private final GameViewCli gameView;
    private final GreenhouseViewCli greenhouseView;
    private final MainMenuViewCli mainMenuView;
    private final ProfileViewCli profileView;
    private final ShopViewCli shopView;
    private final AdventureViewCli adventureView;
    private final PlantSelectionViewCli plantSelectionView;
    private final GamePlayViewCli gamePlayView;
    private final TravelLogViewCli travelLogView;
    private final SettingViewCli settingView;
    private final RegistrationController registrationController;
    private ViewController currentController;

    public CommandParser() {
        UserDatabase userDatabase = UserDatabase.getInstance();
        authView = new AuthViewCli();
        gameView = new GameViewCli();
        greenhouseView = new GreenhouseViewCli();
        mainMenuView = new MainMenuViewCli();
        profileView = new ProfileViewCli();
        shopView = new ShopViewCli();
        adventureView = new AdventureViewCli();
        plantSelectionView = new PlantSelectionViewCli();
        gamePlayView = new GamePlayViewCli();
        travelLogView = new TravelLogViewCli();
        settingView = new SettingViewCli();

        LoginController loginController = new LoginController(userDatabase);
        registrationController = new RegistrationController(userDatabase);

        ProfileController profileController = new ProfileController();
        MainMenuController mainMenuController = new MainMenuController(
                App.getInstance().getCurrentUser(), registrationController, userDatabase);

        loginController.setRegistrationController(registrationController);
        registrationController.setLoginController(loginController);
        profileController.setMainMenuController(mainMenuController);

        loginController.setView(authView);
        registrationController.setView(authView);
        profileController.setView(profileView);

        User stayLoggedInUser = restoreStayLoggedInUser(userDatabase);
        if (stayLoggedInUser != null) {
            App.getInstance().setCurrentUser(stayLoggedInUser);
            switchController(new MainMenuController(stayLoggedInUser, registrationController, userDatabase));
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

    public ViewController getCurrentController() {
        return currentController;
    }

    public void switchController(ViewController newController) {
        if (newController == null) {
            return;
        }

        this.currentController = newController;
        newController.setParser(this);
        if (newController instanceof MainMenuController) {
            newController.setView(mainMenuView);
        } else if (newController instanceof ProfileController) {
            newController.setView(profileView);
        } else if (newController instanceof GameController) {
            newController.setView(gameView);
        } else if (newController instanceof GreenhouseController) {
            newController.setView(greenhouseView);
        } else if (newController instanceof ShopController) {
            newController.setView(shopView);
        } else if (newController instanceof AdventureController) {
            newController.setView(adventureView);
        } else if (newController instanceof PlantSelectionController) {
            newController.setView(plantSelectionView);
        } else if (newController instanceof GamePlayController) {
            newController.setView(gamePlayView);
        } else if (newController instanceof TravelLogController) {
            newController.setView(travelLogView);
        } else if (newController instanceof SettingController) {
            newController.setView(settingView);
        } else if (newController.getView() == null) {
            newController.setView(authView);
        }
        newController.displayMenu();
    }
}
