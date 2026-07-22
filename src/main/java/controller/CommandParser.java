package controller;

import model.App;
import model.user.User;
import model.user.UserDatabase;
import util.StayLoggedInStorage;
import view.cli.*;
import view.cli.minigame.MiniGameHubViewCli;
import view.cli.minigame.StubMiniGameViewCli;
import view.cli.minigame.VaseBreakerViewCli;

import java.util.Scanner;

public class CommandParser {
    private final AuthViewCli authView;
    private final CollectionViewCli collectionView;
    private final GameViewCli gameView;
    private final GreenhouseViewCli greenhouseView;
    private final MainMenuViewCli mainMenuView;
    private final NewsViewCli newsView;
    private final ProfileViewCli profileView;
    private final ShopViewCli shopView;
    private final AdventureViewCli adventureView;
    private final PlantSelectionViewCli plantSelectionView;
    private final GamePlayViewCli gamePlayView;
    private final SpecialLevelViewCli specialLevelView;
    private final ConveyBeltLevelViewCli conveyBeltLevelView;
    private final LockedPlantsSelectionViewCli lockedPlantsSelectionView;
    private final LockedPlantsLevelViewCli lockedPlantsLevelView;
    private final SaveOurSeedsLevelViewCli saveOurSeedsLevelView;
    private final TimedWarLevelViewCli timedWarLevelView;
    private final NightOpsLevelViewCli nightOpsLevelView;
    private final DeadLineLevelViewCli deadLineLevelView;
    private final LoveYourPlantsLevelViewCli loveYourPlantsLevelView;
    private final PlantWhatYouGetLevelViewCli plantWhatYouGetLevelView;
    private final TravelLogViewCli travelLogView;
    private final MiniGameHubViewCli miniGameHubView;
    private final VaseBreakerViewCli vaseBreakerView;
    private final StubMiniGameViewCli stubMiniGameView;
    private final SettingViewCli settingView;
    private final RegistrationController registrationController;
    private ViewController currentController;

    public CommandParser() {
        UserDatabase userDatabase = UserDatabase.getInstance();
        authView = new AuthViewCli();
        collectionView = new CollectionViewCli();
        gameView = new GameViewCli();
        greenhouseView = new GreenhouseViewCli();
        mainMenuView = new MainMenuViewCli();
        newsView = new NewsViewCli();
        profileView = new ProfileViewCli();
        shopView = new ShopViewCli();
        adventureView = new AdventureViewCli();
        plantSelectionView = new PlantSelectionViewCli();
        gamePlayView = new GamePlayViewCli();
        specialLevelView = new SpecialLevelViewCli();
        conveyBeltLevelView = new ConveyBeltLevelViewCli();
        lockedPlantsSelectionView = new LockedPlantsSelectionViewCli();
        lockedPlantsLevelView = new LockedPlantsLevelViewCli();
        saveOurSeedsLevelView = new SaveOurSeedsLevelViewCli();
        timedWarLevelView = new TimedWarLevelViewCli();
        nightOpsLevelView = new NightOpsLevelViewCli();
        deadLineLevelView = new DeadLineLevelViewCli();
        loveYourPlantsLevelView = new LoveYourPlantsLevelViewCli();
        plantWhatYouGetLevelView = new PlantWhatYouGetLevelViewCli();
        travelLogView = new TravelLogViewCli();
        miniGameHubView = new MiniGameHubViewCli();
        vaseBreakerView = new VaseBreakerViewCli();
        stubMiniGameView = new StubMiniGameViewCli();
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
        } else if (newController instanceof CollectionController) {
            newController.setView(collectionView);
        } else if (newController instanceof GreenhouseController) {
            newController.setView(greenhouseView);
        } else if (newController instanceof ShopController) {
            newController.setView(shopView);
        } else if (newController instanceof AdventureController) {
            newController.setView(adventureView);
        } else if (newController instanceof LockedPlantsSelectionController) {
            newController.setView(lockedPlantsSelectionView);
        } else if (newController instanceof LockedPlantsLevelController) {
            newController.setView(lockedPlantsLevelView);
        } else if (newController instanceof PlantSelectionController) {
            newController.setView(plantSelectionView);
        } else if (newController instanceof ConveyBeltLevelController) {
            newController.setView(conveyBeltLevelView);
        } else if (newController instanceof SaveOurSeedsLevelController) {
            newController.setView(saveOurSeedsLevelView);
        } else if (newController instanceof TimedWarLevelController) {
            newController.setView(timedWarLevelView);
        } else if (newController instanceof NightOpsLevelController) {
            newController.setView(nightOpsLevelView);
        } else if (newController instanceof DeadLineLevelController) {
            newController.setView(deadLineLevelView);
        } else if (newController instanceof LoveYourPlantsLevelController) {
            newController.setView(loveYourPlantsLevelView);
        } else if (newController instanceof PlantWhatYouGetLevelController) {
            newController.setView(plantWhatYouGetLevelView);
        } else if (newController instanceof SpecialLevelController) {
            newController.setView(specialLevelView);
        } else if (newController instanceof GamePlayController) {
            newController.setView(gamePlayView);
        } else if (newController instanceof TravelLogController) {
            newController.setView(travelLogView);
        } else if (newController instanceof MiniGameHubController) {
            newController.setView(miniGameHubView);
        } else if (newController instanceof VaseBreakerController) {
            newController.setView(vaseBreakerView);
        } else if (newController instanceof WalnutBowlingController
                || newController instanceof IZombieController
                || newController instanceof BeghouledController
                || newController instanceof ZombotanyController) {
            newController.setView(stubMiniGameView);
        } else if (newController instanceof SettingController) {
            newController.setView(settingView);
        } else if (newController instanceof NewsController) {
            newController.setView(newsView);
        } else if (newController.getView() == null) {
            newController.setView(authView);
        }
        newController.displayMenu();
    }
}
