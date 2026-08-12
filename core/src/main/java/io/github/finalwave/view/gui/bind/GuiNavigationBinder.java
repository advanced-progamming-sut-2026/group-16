package io.github.finalwave.view.gui.bind;

import io.github.finalwave.controller.LeaderboardController;
import io.github.finalwave.controller.LoginController;
import io.github.finalwave.controller.MainMenuController;
import io.github.finalwave.controller.NavigationBinder;
import io.github.finalwave.controller.NewsController;
import io.github.finalwave.controller.RegistrationController;
import io.github.finalwave.controller.SettingController;
import io.github.finalwave.controller.ViewController;
import io.github.finalwave.view.gui.AuthViewGui;
import io.github.finalwave.view.gui.LeaderboardViewGui;
import io.github.finalwave.view.gui.MainMenuViewGui;
import io.github.finalwave.view.gui.NewsViewGui;
import io.github.finalwave.view.gui.screen.ScreenRouter;
import io.github.finalwave.view.gui.SettingViewGui;


public final class GuiNavigationBinder implements NavigationBinder {
    private final ScreenRouter router;
    private final AuthViewGui authView;
    private final MainMenuViewGui mainMenuView;
    private final NewsViewGui newsView;
    private final LeaderboardViewGui leaderboardView;
    private final SettingViewGui settingView;

    public GuiNavigationBinder(ScreenRouter router) {
        this.router = router;
        this.authView = new AuthViewGui(router);
        this.mainMenuView = new MainMenuViewGui(router);
        this.newsView = new NewsViewGui(router);
        this.leaderboardView = new LeaderboardViewGui(router);
        this.settingView = new SettingViewGui(router);
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
