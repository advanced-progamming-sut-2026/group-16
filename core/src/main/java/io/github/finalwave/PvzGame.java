package io.github.finalwave;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import io.github.finalwave.controller.AppBootstrap;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.bind.GuiNavigationBinder;
import io.github.finalwave.view.gui.screen.BootScreen;
import io.github.finalwave.view.gui.screen.ScreenRouter;
import io.github.finalwave.model.user.UserDatabase;


public final class PvzGame extends Game {
    private GameAssets assets;
    private ScreenRouter router;
    private AppBootstrap bootstrap;
    private BootScreen bootScreen;
    private boolean applicationStarted;

    @Override
    public void create() {
        assets = new GameAssets(Gdx.files.local("."));
        router = new ScreenRouter(this);
        GuiNavigationBinder binder = new GuiNavigationBinder(router);
        bootstrap = new AppBootstrap(UserDatabase.getInstance(), binder);
        bootScreen = new BootScreen(this);
        setScreen(bootScreen);
    }


    public void startApplication() {
        if (applicationStarted) {
            return;
        }
        applicationStarted = true;
        bootstrap.start();
    }

    public GameAssets assets() {
        return assets;
    }

    public ScreenRouter router() {
        return router;
    }

    public AppBootstrap bootstrap() {
        return bootstrap;
    }


    public void installScreen(Screen screen) {
        this.screen = screen;
    }

    @Override
    public void render() {
        if (assets != null) {
            assets.update();
        }
        super.render();
    }

    @Override
    public void dispose() {
        if (getScreen() != null) {
            getScreen().hide();
        }
        if (bootScreen != null) {
            bootScreen.dispose();
        }
        if (router != null) {
            router.dispose();
        }
        if (assets != null) {
            assets.dispose();
        }
        super.dispose();
    }
}
