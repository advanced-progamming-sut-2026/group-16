package io.github.finalwave.view.gui.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import io.github.finalwave.PvzGame;
import io.github.finalwave.controller.LoginController;
import io.github.finalwave.controller.MainMenuController;
import io.github.finalwave.controller.NewsController;
import io.github.finalwave.controller.RegistrationController;
import io.github.finalwave.controller.ViewController;

import java.util.List;


public final class ScreenRouter {
    private static final String TAG = "ScreenRouter";

    private final PvzGame game;
    private Screen current;
    private boolean transitioning;
    private SignupScreen signupScreen;
    private LoginScreen loginScreen;
    private MainMenuScreen mainMenuScreen;
    private NewsScreen newsScreen;

    public ScreenRouter(PvzGame game) {
        this.game = game;
    }

    public boolean supports(ViewController controller) {
        return controller instanceof RegistrationController
                || controller instanceof LoginController
                || controller instanceof MainMenuController
                || controller instanceof NewsController;
    }

    public boolean supportsDestination(MainMenuController.Destination destination) {
        return destination == MainMenuController.Destination.NEWS;
    }

    public void showFor(ViewController controller) {
        if (controller instanceof RegistrationController registrationController) {
            showSignup(registrationController);
        } else if (controller instanceof LoginController loginController) {
            showLogin(loginController);
        } else if (controller instanceof MainMenuController mainMenuController) {
            showMainMenu(mainMenuController);
        } else if (controller instanceof NewsController newsController) {
            showNews(newsController);
        } else {
            Gdx.app.log(TAG, "No GUI screen registered for " + controller.getClass().getSimpleName());
        }
    }

    public void showSignup(RegistrationController controller) {
        if (signupScreen == null) {
            signupScreen = new SignupScreen(game);
        }
        signupScreen.bind(controller);
        setScreen(signupScreen);
    }

    public void showLogin(LoginController controller) {
        if (loginScreen == null) {
            loginScreen = new LoginScreen(game);
        }
        loginScreen.bind(controller);
        setScreen(loginScreen);
    }

    public void showMainMenu(MainMenuController controller) {
        if (mainMenuScreen == null) {
            mainMenuScreen = new MainMenuScreen(game);
        }
        mainMenuScreen.bind(controller);
        setScreen(mainMenuScreen);
    }

    public void showNews(NewsController controller) {
        if (newsScreen == null) {
            newsScreen = new NewsScreen(game);
        }
        newsScreen.bind(controller);
        setScreen(newsScreen);
    }

    public MenuScreen currentMenuScreen() {
        Screen active = game.getScreen();
        if (active instanceof MenuScreen menuScreen) {
            return menuScreen;
        }
        if (current instanceof MenuScreen menuScreen) {
            return menuScreen;
        }
        return null;
    }


    public void openSignupSecurityModal() {
        if (signupScreen != null) {
            signupScreen.openSecurityQuestionModal();
        }
    }

    public void showLoginPasswordResetStep() {
        if (loginScreen != null) {
            loginScreen.showPasswordResetStep();
        }
    }

    public void closeLoginForgotPasswordModal() {
        if (loginScreen != null) {
            loginScreen.closeForgotPasswordModal();
        }
    }

    public void updateMainMenuHeader(String nickname, boolean hasUnreadNews) {
        if (mainMenuScreen != null) {
            mainMenuScreen.updateHeader(nickname, hasUnreadNews);
        }
    }

    public void showNewsPlaceholder(String message) {
        if (newsScreen != null) {
            newsScreen.showPlaceholder(message);
        }
    }

    public void showNewsLines(List<String> lines) {
        if (newsScreen != null) {
            newsScreen.showNewsLines(lines);
        }
    }

    public void dispose() {
        if (signupScreen != null) {
            signupScreen.dispose();
        }
        if (loginScreen != null) {
            loginScreen.dispose();
        }
        if (mainMenuScreen != null) {
            mainMenuScreen.dispose();
        }
        if (newsScreen != null) {
            newsScreen.dispose();
        }
    }

    private void setScreen(Screen screen) {
        Screen active = game.getScreen();

        if (screen == current && active == screen) {
            game.setScreen(screen);
            return;
        }

        if (active == null
                || active instanceof FadeTransitionScreen
                || transitioning
                || screen == active) {
            current = screen;
            transitioning = false;
            game.setScreen(screen);
            return;
        }

        TextureRegion snapshot = FadeTransitionScreen.captureFramebuffer();
        transitioning = true;
        current = screen;
        FadeTransitionScreen fade = new FadeTransitionScreen(game, snapshot, screen, () -> transitioning = false);
        game.setScreen(fade);
    }
}
