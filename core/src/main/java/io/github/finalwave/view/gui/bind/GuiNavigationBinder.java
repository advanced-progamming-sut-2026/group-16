package io.github.finalwave.view.gui.bind;

import io.github.finalwave.controller.AdventureController;
import io.github.finalwave.controller.CollectionController;
import io.github.finalwave.controller.GameController;
import io.github.finalwave.controller.GamePlayController;
import io.github.finalwave.controller.GreenhouseController;
import io.github.finalwave.controller.LeaderboardController;
import io.github.finalwave.controller.LoginController;
import io.github.finalwave.controller.MainMenuController;
import io.github.finalwave.controller.MiniGameHubController;
import io.github.finalwave.controller.NavigationBinder;
import io.github.finalwave.controller.NewsController;
import io.github.finalwave.controller.PlantSelectionController;
import io.github.finalwave.controller.ProfileController;
import io.github.finalwave.controller.RegistrationController;
import io.github.finalwave.controller.SettingController;
import io.github.finalwave.controller.ShopController;
import io.github.finalwave.controller.TravelLogController;
import io.github.finalwave.controller.VaseBreakerController;
import io.github.finalwave.controller.ViewController;
import io.github.finalwave.view.gui.AdventureViewGui;
import io.github.finalwave.view.gui.AuthViewGui;
import io.github.finalwave.view.gui.CollectionViewGui;
import io.github.finalwave.view.gui.ComingSoonViewGui;
import io.github.finalwave.view.gui.GamePlayViewGui;
import io.github.finalwave.view.gui.GameViewGui;
import io.github.finalwave.view.gui.GreenhouseViewGui;
import io.github.finalwave.view.gui.LeaderboardViewGui;
import io.github.finalwave.view.gui.MainMenuViewGui;
import io.github.finalwave.view.gui.MiniGameHubViewGui;
import io.github.finalwave.view.gui.NewsViewGui;
import io.github.finalwave.view.gui.PlantSelectionViewGui;
import io.github.finalwave.view.gui.ProfileViewGui;
import io.github.finalwave.view.gui.SettingViewGui;
import io.github.finalwave.view.gui.ShopViewGui;
import io.github.finalwave.view.gui.TravelLogViewGui;
import io.github.finalwave.view.gui.VaseBreakerViewGui;
import io.github.finalwave.view.gui.screen.ScreenRouter;

public final class GuiNavigationBinder implements NavigationBinder {
    private final ScreenRouter router;
    private final AuthViewGui authView;
    private final MainMenuViewGui mainMenuView;
    private final NewsViewGui newsView;
    private final LeaderboardViewGui leaderboardView;
    private final SettingViewGui settingView;
    private final GreenhouseViewGui greenhouseView;
    private final ProfileViewGui profileView;
    private final ShopViewGui shopView;
    private final CollectionViewGui collectionView;
    private final GameViewGui gameView;
    private final AdventureViewGui adventureView;
    private final TravelLogViewGui travelLogView;
    private final PlantSelectionViewGui plantSelectionView;
    private final GamePlayViewGui gamePlayView;
    private final MiniGameHubViewGui miniGameHubView;
    private final VaseBreakerViewGui vaseBreakerView;
    private final ComingSoonViewGui comingSoonView;

    public GuiNavigationBinder(ScreenRouter router) {
        this.router = router;
        this.authView = new AuthViewGui(router);
        this.mainMenuView = new MainMenuViewGui(router);
        this.newsView = new NewsViewGui(router);
        this.leaderboardView = new LeaderboardViewGui(router);
        this.settingView = new SettingViewGui(router);
        this.greenhouseView = new GreenhouseViewGui(router);
        this.profileView = new ProfileViewGui(router);
        this.shopView = new ShopViewGui(router);
        this.collectionView = new CollectionViewGui(router);
        this.gameView = new GameViewGui(router);
        this.adventureView = new AdventureViewGui(router);
        this.travelLogView = new TravelLogViewGui(router);
        this.plantSelectionView = new PlantSelectionViewGui(router);
        this.gamePlayView = new GamePlayViewGui(router);
        this.miniGameHubView = new MiniGameHubViewGui(router);
        this.vaseBreakerView = new VaseBreakerViewGui(router);
        this.comingSoonView = new ComingSoonViewGui(router);
    }

    @Override
    public void bind(ViewController newController) {
        if (newController instanceof MainMenuController mainMenuController) {
            mainMenuView.bindController(mainMenuController);
            newController.setView(mainMenuView);
        } else if (newController instanceof NewsController newsController) {
            newsView.bindController(newsController);
            newController.setView(newsView);
        } else if (newController instanceof LeaderboardController leaderboardController) {
            leaderboardView.bindController(leaderboardController);
            newController.setView(leaderboardView);
        } else if (newController instanceof SettingController settingController) {
            settingView.bindController(settingController);
            newController.setView(settingView);
        } else if (newController instanceof GreenhouseController greenhouseController) {
            greenhouseView.bindController(greenhouseController);
            newController.setView(greenhouseView);
        } else if (newController instanceof ProfileController profileController) {
            profileView.bindController(profileController);
            newController.setView(profileView);
        } else if (newController instanceof ShopController shopController) {
            shopView.bindController(shopController);
            newController.setView(shopView);
        } else if (newController instanceof CollectionController collectionController) {
            collectionView.bindController(collectionController);
            newController.setView(collectionView);
        } else if (newController instanceof GameController gameController) {
            gameView.bindController(gameController);
            newController.setView(gameView);
        } else if (newController instanceof AdventureController adventureController) {
            adventureView.bindController(adventureController);
            newController.setView(adventureView);
        } else if (newController instanceof TravelLogController travelLogController) {
            travelLogView.bindController(travelLogController);
            newController.setView(travelLogView);
        } else if (newController instanceof PlantSelectionController plantSelectionController) {
            plantSelectionView.bindController(plantSelectionController);
            newController.setView(plantSelectionView);
        } else if (newController instanceof GamePlayController gamePlayController) {
            gamePlayView.bindController(gamePlayController);
            newController.setView(gamePlayView);
        } else if (newController instanceof MiniGameHubController miniGameHubController) {
            miniGameHubView.bindController(miniGameHubController);
            newController.setView(miniGameHubView);
        } else if (newController instanceof VaseBreakerController vaseBreakerController) {
            vaseBreakerView.bindController(vaseBreakerController);
            newController.setView(vaseBreakerView);
        } else if (newController instanceof RegistrationController
                || newController instanceof LoginController) {
            authView.bindController(newController);
            newController.setView(authView);
        } else if (newController.getView() == null) {
            authView.bindController(newController);
            newController.setView(authView);
        }

        if (router.supports(newController)) {
            router.showFor(newController);
        }
    }
}
