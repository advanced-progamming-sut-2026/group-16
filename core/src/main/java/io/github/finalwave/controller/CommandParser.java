package io.github.finalwave.controller;

import io.github.finalwave.model.user.UserDatabase;
import io.github.finalwave.login.LocalLoginGateway;
import io.github.finalwave.registration.LocalRegistrationGateway;
import io.github.finalwave.registration.RegistrationGateway;
import io.github.finalwave.view.cli.*;
import io.github.finalwave.view.cli.minigame.BeghouledViewCli;
import io.github.finalwave.view.cli.minigame.IZombieViewCli;
import io.github.finalwave.view.cli.minigame.MiniGameHubViewCli;
import io.github.finalwave.view.cli.minigame.VaseBreakerViewCli;
import io.github.finalwave.view.cli.minigame.WalnutBowlingViewCli;
import io.github.finalwave.view.cli.minigame.ZombotanyViewCli;

import java.util.Scanner;

public class CommandParser implements NavigationBinder {
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
    private final WalnutBowlingViewCli walnutBowlingView;
    private final IZombieViewCli iZombieView;
    private final BeghouledViewCli beghouledView;
    private final ZombotanyViewCli zombotanyView;
    private final SettingViewCli settingView;
    private final LeaderboardViewCli leaderboardView;
    private final ScoreGameViewCli scoreGameView;
    private final RegistrationController registrationController;
    private final AppBootstrap bootstrap;

    public CommandParser() {
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
        walnutBowlingView = new WalnutBowlingViewCli();
        iZombieView = new IZombieViewCli();
        beghouledView = new BeghouledViewCli();
        zombotanyView = new ZombotanyViewCli();
        settingView = new SettingViewCli();
        leaderboardView = new LeaderboardViewCli();
        scoreGameView = new ScoreGameViewCli();
        UserDatabase userDatabase = UserDatabase.getInstance();
        RegistrationGateway registrationGateway = new LocalRegistrationGateway(userDatabase);
        io.github.finalwave.login.LoginGateway loginGateway = new LocalLoginGateway(userDatabase);
        registrationController = new RegistrationController(registrationGateway, userDatabase, loginGateway);
        bootstrap = new AppBootstrap(userDatabase, registrationGateway, loginGateway, this, true);
        bootstrap.start();
    }

    public void run() {
        try (Scanner scanner = new Scanner(System.in)) {
            while (scanner.hasNextLine()) {
                parseAndExecute(scanner.nextLine());
            }
        }
    }

    public void parseAndExecute(String input) {
        ViewController current = bootstrap.navigator().current();
        if (input == null || current == null) {
            return;
        }

        String trimmed = input.trim();
        if (trimmed.isEmpty()) {
            return;
        }
        current.handleCommand(trimmed);
    }

    public ViewController getCurrentController() {
        return bootstrap.navigator().current();
    }

    public Navigator getNavigator() {
        return bootstrap.navigator();
    }

    public RegistrationController getRegistrationController() {
        return registrationController;
    }

    @Override
    public void bind(ViewController newController) {
        if (bindMenuViews(newController) || bindAdventureViews(newController)
                || bindSpecialLevelViews(newController) || bindMiniGameViews(newController)
                || bindMiscViews(newController)) {
            return;
        }
        if (newController.getView() == null) {
            newController.setView(authView);
        }
    }

    private boolean bindMenuViews(ViewController newController) {
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
        } else {
            return false;
        }
        return true;
    }

    private boolean bindAdventureViews(ViewController newController) {
        if (newController instanceof AdventureController) {
            newController.setView(adventureView);
        } else if (newController instanceof LockedPlantsSelectionController) {
            newController.setView(lockedPlantsSelectionView);
        } else if (newController instanceof LockedPlantsLevelController) {
            newController.setView(lockedPlantsLevelView);
        } else if (newController instanceof PlantSelectionController) {
            newController.setView(plantSelectionView);
        } else {
            return false;
        }
        return true;
    }

    private boolean bindSpecialLevelViews(ViewController newController) {
        if (newController instanceof ConveyBeltLevelController) {
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
        } else {
            return false;
        }
        return true;
    }

    private boolean bindMiniGameViews(ViewController newController) {
        if (newController instanceof TravelLogController) {
            newController.setView(travelLogView);
        } else if (newController instanceof MiniGameHubController) {
            newController.setView(miniGameHubView);
        } else if (newController instanceof VaseBreakerController) {
            newController.setView(vaseBreakerView);
        } else if (newController instanceof WalnutBowlingController) {
            newController.setView(walnutBowlingView);
        } else if (newController instanceof IZombieController) {
            newController.setView(iZombieView);
        } else if (newController instanceof BeghouledController) {
            newController.setView(beghouledView);
        } else if (newController instanceof ZombotanyController) {
            newController.setView(zombotanyView);
        } else {
            return false;
        }
        return true;
    }

    private boolean bindMiscViews(ViewController newController) {
        if (newController instanceof SettingController) {
            newController.setView(settingView);
        } else if (newController instanceof NewsController) {
            newController.setView(newsView);
        } else if (newController instanceof LeaderboardController) {
            newController.setView(leaderboardView);
        } else if (newController instanceof ScoreGameController) {
            newController.setView(scoreGameView);
        } else if (newController instanceof ScoreGamePlantSelectionController) {
            newController.setView(plantSelectionView);
        } else {
            return false;
        }
        return true;
    }
}
