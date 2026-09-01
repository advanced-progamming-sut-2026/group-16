package io.github.finalwave.view.gui.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import io.github.finalwave.PvzGame;
import io.github.finalwave.controller.AdventureController;
import io.github.finalwave.controller.BeghouledController;
import io.github.finalwave.controller.CollectionController;
import io.github.finalwave.controller.CouchIZombieController;
import io.github.finalwave.controller.GameController;
import io.github.finalwave.controller.GamePlayController;
import io.github.finalwave.controller.GreenhouseController;
import io.github.finalwave.controller.IZombieController;
import io.github.finalwave.controller.IZombieMatchmakingController;
import io.github.finalwave.network.match.ListMatchUsersResponse;
import io.github.finalwave.controller.LeaderboardController;
import io.github.finalwave.controller.LoginController;
import io.github.finalwave.controller.MainMenuController;
import io.github.finalwave.controller.MiniGameHubController;
import io.github.finalwave.controller.NetworkedIZombieController;
import io.github.finalwave.controller.NewsController;
import io.github.finalwave.controller.PlantSelectionController;
import io.github.finalwave.controller.ProfileController;
import io.github.finalwave.controller.RegistrationController;
import io.github.finalwave.controller.ScoreGameController;
import io.github.finalwave.controller.SettingController;
import io.github.finalwave.controller.ShopController;
import io.github.finalwave.controller.TravelLogController;
import io.github.finalwave.controller.VaseBreakerController;
import io.github.finalwave.controller.ViewController;
import io.github.finalwave.controller.WalnutBowlingController;
import io.github.finalwave.controller.ZombotanyController;
import io.github.finalwave.model.game.MatchResult;
import io.github.finalwave.model.leaderboard.LeaderboardEntry;
import io.github.finalwave.model.leaderboard.LeaderboardSortColumn;
import io.github.finalwave.model.scoregame.MeowPointBreakdown;
import io.github.finalwave.model.user.User;

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
    private LeaderboardScreen leaderboardScreen;
    private SettingScreen settingScreen;
    private GreenhouseScreen greenhouseScreen;
    private ProfileScreen profileScreen;
    private ShopScreen shopScreen;
    private CollectionScreen collectionScreen;
    private ChapterSelectScreen chapterSelectScreen;
    private AdventureScreen adventureScreen;
    private TravelLogScreen travelLogScreen;
    private PlantSelectionScreen plantSelectionScreen;
    private GamePlayScreen gamePlayScreen;
    private MiniGameHubScreen miniGameHubScreen;
    private IZombieMatchmakingScreen iZombieMatchmakingScreen;
    private ComingSoonScreen comingSoonScreen;
    private ScoreGameScreen scoreGameScreen;

    public ScreenRouter(PvzGame game) {
        this.game = game;
    }

    public boolean supports(ViewController controller) {
        return controller instanceof RegistrationController
                || controller instanceof LoginController
                || controller instanceof MainMenuController
                || controller instanceof NewsController
                || controller instanceof LeaderboardController
                || controller instanceof SettingController
                || controller instanceof GreenhouseController
                || controller instanceof ProfileController
                || controller instanceof ShopController
                || controller instanceof CollectionController
                || controller instanceof GameController
                || controller instanceof ScoreGameController
                || controller instanceof AdventureController
                || controller instanceof TravelLogController
                || controller instanceof PlantSelectionController
                || controller instanceof GamePlayController
                || controller instanceof MiniGameHubController
                || controller instanceof VaseBreakerController
                || controller instanceof WalnutBowlingController
                || controller instanceof IZombieController
                || controller instanceof NetworkedIZombieController
                || controller instanceof CouchIZombieController
                || controller instanceof IZombieMatchmakingController
                || controller instanceof BeghouledController
                || controller instanceof ZombotanyController;
    }

    public boolean supportsDestination(MainMenuController.Destination destination) {
        return destination == MainMenuController.Destination.NEWS
                || destination == MainMenuController.Destination.LEADERBOARD
                || destination == MainMenuController.Destination.SETTINGS
                || destination == MainMenuController.Destination.GREENHOUSE
                || destination == MainMenuController.Destination.PROFILE
                || destination == MainMenuController.Destination.GAME
                || destination == MainMenuController.Destination.SCORE_GAME;
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
        } else if (controller instanceof LeaderboardController leaderboardController) {
            showLeaderboard(leaderboardController);
        } else if (controller instanceof SettingController settingController) {
            showSettings(settingController);
        } else if (controller instanceof GreenhouseController greenhouseController) {
            showGreenhouse(greenhouseController);
        } else if (controller instanceof ProfileController profileController) {
            showProfile(profileController);
        } else if (controller instanceof ShopController shopController) {
            showShop(shopController);
        } else if (controller instanceof CollectionController collectionController) {
            showCollection(collectionController);
        } else if (controller instanceof GameController gameController) {
            showChapterSelect(gameController);
        } else if (controller instanceof ScoreGameController scoreGameController) {
            showScoreGame(scoreGameController);
        } else if (controller instanceof AdventureController adventureController) {
            showAdventure(adventureController);
        } else if (controller instanceof TravelLogController travelLogController) {
            showTravelLog(travelLogController);
        } else if (controller instanceof PlantSelectionController plantSelectionController) {
            showPlantSelection(plantSelectionController);
        } else if (controller instanceof GamePlayController gamePlayController) {
            showGamePlay(gamePlayController);
        } else if (controller instanceof MiniGameHubController miniGameHubController) {
            showMiniGameHub(miniGameHubController);
        } else if (controller instanceof VaseBreakerController vaseBreakerController) {
            showGamePlay(vaseBreakerController);
        } else if (controller instanceof WalnutBowlingController walnutBowlingController) {
            showGamePlay(walnutBowlingController);
        } else if (controller instanceof IZombieController iZombieController) {
            showGamePlay(iZombieController);
        } else if (controller instanceof NetworkedIZombieController networkedIZombieController) {
            showGamePlay(networkedIZombieController);
        } else if (controller instanceof CouchIZombieController couchIZombieController) {
            showGamePlay(couchIZombieController);
        } else if (controller instanceof IZombieMatchmakingController matchmakingController) {
            showIZombieMatchmaking(matchmakingController);
        } else if (controller instanceof BeghouledController beghouledController) {
            showGamePlay(beghouledController);
        } else if (controller instanceof ZombotanyController zombotanyController) {
            showGamePlay(zombotanyController);
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

    public void showLeaderboard(LeaderboardController controller) {
        if (leaderboardScreen == null) {
            leaderboardScreen = new LeaderboardScreen(game);
        }
        leaderboardScreen.bind(controller);
        setScreen(leaderboardScreen);
    }

    public void showSettings(SettingController controller) {
        if (settingScreen == null) {
            settingScreen = new SettingScreen(game);
        }
        settingScreen.bind(controller);
        setScreen(settingScreen);
    }

    public void showGreenhouse(GreenhouseController controller) {
        if (greenhouseScreen == null) {
            greenhouseScreen = new GreenhouseScreen(game);
        }
        greenhouseScreen.bind(controller);
        setScreen(greenhouseScreen);
    }

    public void showProfile(ProfileController controller) {
        if (profileScreen == null) {
            profileScreen = new ProfileScreen(game);
        }
        profileScreen.bind(controller);
        setScreen(profileScreen);
    }

    public void showShop(ShopController controller) {
        if (shopScreen == null) {
            shopScreen = new ShopScreen(game);
        }
        shopScreen.bind(controller);
        setScreen(shopScreen);
    }

    public void showCollection(CollectionController controller) {
        if (collectionScreen == null) {
            collectionScreen = new CollectionScreen(game);
        }
        collectionScreen.bind(controller);
        setScreen(collectionScreen);
    }

    public void showChapterSelect(GameController controller) {
        if (chapterSelectScreen == null) {
            chapterSelectScreen = new ChapterSelectScreen(game);
        }
        chapterSelectScreen.bind(controller);
        setScreen(chapterSelectScreen);
    }

    public void showScoreGame(ScoreGameController controller) {
        if (scoreGameScreen == null) {
            scoreGameScreen = new ScoreGameScreen(game);
        }
        scoreGameScreen.bind(controller);
        setScreen(scoreGameScreen);
    }

    public void refreshScoreGame() {
        if (scoreGameScreen != null && current == scoreGameScreen) {
            scoreGameScreen.show();
        }
    }

    public void showScoreGameResult(MeowPointBreakdown breakdown, Integer bestMeowPoint, boolean newBest) {
        if (scoreGameScreen != null) {
            scoreGameScreen.showResult(breakdown, bestMeowPoint, newBest);
        }
    }

    public void showAdventure(AdventureController controller) {
        if (adventureScreen == null) {
            adventureScreen = new AdventureScreen(game);
        }
        adventureScreen.bind(controller);
        setScreen(adventureScreen);
    }

    public void showTravelLog(TravelLogController controller) {
        if (travelLogScreen == null) {
            travelLogScreen = new TravelLogScreen(game);
        }
        travelLogScreen.bind(controller);
        setScreen(travelLogScreen);
    }

    public void showPlantSelection(PlantSelectionController controller) {
        if (plantSelectionScreen == null) {
            plantSelectionScreen = new PlantSelectionScreen(game);
        }
        plantSelectionScreen.bind(controller);
        setScreen(plantSelectionScreen);
    }

    public void showGamePlay(GamePlayController controller) {
        if (gamePlayScreen == null) {
            gamePlayScreen = new GamePlayScreen(game);
        }
        gamePlayScreen.bind(controller);
        setScreen(gamePlayScreen);
    }

    public void showGamePlay(VaseBreakerController controller) {
        if (gamePlayScreen == null) {
            gamePlayScreen = new GamePlayScreen(game);
        }
        gamePlayScreen.bind(controller);
        setScreen(gamePlayScreen);
    }

    public void showGamePlay(WalnutBowlingController controller) {
        if (gamePlayScreen == null) {
            gamePlayScreen = new GamePlayScreen(game);
        }
        gamePlayScreen.bind(controller);
        setScreen(gamePlayScreen);
    }

    public void showGamePlay(IZombieController controller) {
        if (gamePlayScreen == null) {
            gamePlayScreen = new GamePlayScreen(game);
        }
        gamePlayScreen.bind(controller);
        setScreen(gamePlayScreen);
    }

    public void showGamePlay(NetworkedIZombieController controller) {
        if (gamePlayScreen == null) {
            gamePlayScreen = new GamePlayScreen(game);
        }
        gamePlayScreen.bind(controller);
        setScreen(gamePlayScreen);
    }

    public void showGamePlay(CouchIZombieController controller) {
        if (gamePlayScreen == null) {
            gamePlayScreen = new GamePlayScreen(game);
        }
        gamePlayScreen.bind(controller);
        setScreen(gamePlayScreen);
    }

    public void showIZombieMatchmaking(IZombieMatchmakingController controller) {
        if (iZombieMatchmakingScreen == null) {
            iZombieMatchmakingScreen = new IZombieMatchmakingScreen(game);
        }
        iZombieMatchmakingScreen.bind(controller);
        setScreen(iZombieMatchmakingScreen);
    }

    public void refreshIZombieMatchmaking() {
        if (iZombieMatchmakingScreen != null) {
            iZombieMatchmakingScreen.refresh();
        }
    }

    public void showIZombieMatchmakingSearching(boolean searching) {
        if (iZombieMatchmakingScreen != null) {
            iZombieMatchmakingScreen.showSearching(searching);
        }
    }

    public void showChallengeInvite(String inviteId, String fromUsername) {
        if (iZombieMatchmakingScreen != null) {
            iZombieMatchmakingScreen.showInvite(inviteId, fromUsername);
        }
    }

    public void hideChallengeInvite() {
        if (iZombieMatchmakingScreen != null) {
            iZombieMatchmakingScreen.hideInvite();
        }
    }

    public void toastMatchmakingError(String message) {
        if (iZombieMatchmakingScreen != null) {
            iZombieMatchmakingScreen.toastError(message);
        }
    }

    public void toastMatchmakingMessage(String message) {
        if (iZombieMatchmakingScreen != null) {
            iZombieMatchmakingScreen.toastMessage(message);
        }
    }

    public void showIZombieMatchmakingDirectory(ListMatchUsersResponse response) {
        if (iZombieMatchmakingScreen != null) {
            iZombieMatchmakingScreen.updatePlayerDirectory(response);
        }
    }

    public void selectIZombieMatchmakingUsername(String username) {
        if (iZombieMatchmakingScreen != null) {
            iZombieMatchmakingScreen.selectUsername(username);
        }
    }

    public void showGamePlay(BeghouledController controller) {
        if (gamePlayScreen == null) {
            gamePlayScreen = new GamePlayScreen(game);
        }
        gamePlayScreen.bind(controller);
        setScreen(gamePlayScreen);
    }

    public void showGamePlay(ZombotanyController controller) {
        if (gamePlayScreen == null) {
            gamePlayScreen = new GamePlayScreen(game);
        }
        gamePlayScreen.bind(controller);
        setScreen(gamePlayScreen);
    }

    public void showMiniGameHub(MiniGameHubController controller) {
        if (miniGameHubScreen == null) {
            miniGameHubScreen = new MiniGameHubScreen(game);
        }
        miniGameHubScreen.bind(controller);
        setScreen(miniGameHubScreen);
    }

    public void refreshMiniGameHub() {
        if (miniGameHubScreen != null) {
            miniGameHubScreen.refresh();
        }
    }

    public void showComingSoon(Runnable onBack, String title) {
        if (comingSoonScreen == null) {
            comingSoonScreen = new ComingSoonScreen(game);
        }
        comingSoonScreen.bind(onBack, title);
        setScreen(comingSoonScreen);
    }

    public GamePlayScreen currentGamePlayScreen() {
        Screen active = game.getScreen();
        if (active instanceof GamePlayScreen screen) {
            return screen;
        }
        if (current instanceof GamePlayScreen screen) {
            return screen;
        }
        return gamePlayScreen;
    }

    public void toastGamePlayError(String message) {
        if (gamePlayScreen != null) {
            gamePlayScreen.toastError(message);
        }
    }

    public void toastGamePlayMessage(String message) {
        if (gamePlayScreen != null) {
            gamePlayScreen.toastMessage(message);
        }
    }

    public void showGamePlayAlert(String message) {
        if (gamePlayScreen != null) {
            gamePlayScreen.showAlert(message);
        }
    }

    public void showGamePlayWaveAlert(int waveNumber, boolean finalWave) {
        if (gamePlayScreen != null) {
            gamePlayScreen.showWaveAlert(waveNumber, finalWave);
        }
    }

    public void playGamePlayStartChant() {
        if (gamePlayScreen != null) {
            gamePlayScreen.playStartChant(null);
        }
    }

    public void showGamePlayPauseModal() {
        if (gamePlayScreen != null) {
            gamePlayScreen.showPauseModal();
        }
    }

    public void hideGamePlayPauseModal() {
        if (gamePlayScreen != null) {
            gamePlayScreen.hidePauseModal();
        }
    }

    public void showGamePlayResult(MatchResult result) {
        if (gamePlayScreen != null) {
            gamePlayScreen.showResult(result);
        }
    }

    public void refreshGamePlayHud() {
        if (gamePlayScreen != null) {
            gamePlayScreen.refreshHud();
        }
    }

    public void playPlantSfx() {
        game.assets().audio().playPlant();
    }

    public void playPlantWaterSfx() {
        game.assets().audio().playPlantWater();
    }

    public void playShovelSfx() {
        game.assets().audio().playShovel();
    }

    public void playBowlingSpawnSfx() {
        game.assets().audio().playPlantBowling();
    }

    public void playBowlingImpactSfx() {
        game.assets().audio().playBowlingImpact();
    }

    public void playExplosionSfx() {
        game.assets().audio().playExplosion();
    }

    public void dismissGamePlayResult() {
        if (gamePlayScreen != null) {
            gamePlayScreen.dismissResult();
        }
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

    public void showSignupInlineError(String message) {
        if (signupScreen != null) {
            signupScreen.closeSecurityQuestionModal();
            signupScreen.showInlineError(message);
        }
    }

    public void clearSignupInlineError() {
        if (signupScreen != null) {
            signupScreen.clearInlineError();
        }
    }

    public void showLoginInlineError(String message) {
        if (loginScreen != null) {
            loginScreen.closeForgotPasswordModal();
            loginScreen.showInlineError(message);
        }
    }

    public void clearLoginInlineError() {
        if (loginScreen != null) {
            loginScreen.clearInlineError();
        }
    }

    public void showLoginPasswordResetStep() {
        if (loginScreen != null) {
            loginScreen.showPasswordResetStep();
        }
    }

    public void showLoginPasswordSecurityStep() {
        if (loginScreen != null) {
            loginScreen.showSecurityQuestionStep();
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

    public void showLeaderboardTable(
            List<LeaderboardEntry> entries,
            LeaderboardSortColumn column,
            boolean ascending) {
        if (leaderboardScreen != null) {
            leaderboardScreen.showLeaderboard(entries, column, ascending);
        }
    }

    public void refreshSettingsForm() {
        if (settingScreen != null) {
            settingScreen.refreshForm();
        }
    }

    public void refreshGreenhouse() {
        if (greenhouseScreen != null) {
            greenhouseScreen.refreshPots();
        }
    }

    public void showGreenhouseCollectReward(String reward) {
        if (greenhouseScreen != null) {
            greenhouseScreen.showCollectReward(reward);
        }
    }

    public void refreshProfile(User user) {
        if (profileScreen != null) {
            profileScreen.refreshFromUser(user);
        }
    }

    public void closeProfilePasswordModal() {
        if (profileScreen != null) {
            profileScreen.closePasswordModal();
        }
    }

    public void refreshShop() {
        if (shopScreen != null) {
            shopScreen.refreshOffers();
        }
    }

    public void refreshCollection() {
        if (collectionScreen != null) {
            collectionScreen.refresh();
        }
    }

    public void refreshChapterSelect() {
        if (chapterSelectScreen != null) {
            chapterSelectScreen.refreshCarousel();
        }
    }

    public void refreshAdventureMap() {
        if (adventureScreen != null) {
            adventureScreen.refreshPath();
        }
    }

    public void refreshTravelLog() {
        if (travelLogScreen != null) {
            travelLogScreen.refresh();
        }
    }

    public void refreshPlantSelection() {
        if (plantSelectionScreen != null) {
            plantSelectionScreen.refresh();
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
        if (leaderboardScreen != null) {
            leaderboardScreen.dispose();
        }
        if (settingScreen != null) {
            settingScreen.dispose();
        }
        if (greenhouseScreen != null) {
            greenhouseScreen.dispose();
        }
        if (profileScreen != null) {
            profileScreen.dispose();
        }
        if (shopScreen != null) {
            shopScreen.dispose();
        }
        if (collectionScreen != null) {
            collectionScreen.dispose();
        }
        if (chapterSelectScreen != null) {
            chapterSelectScreen.dispose();
        }
        if (adventureScreen != null) {
            adventureScreen.dispose();
        }
        if (travelLogScreen != null) {
            travelLogScreen.dispose();
        }
        if (plantSelectionScreen != null) {
            plantSelectionScreen.dispose();
        }
        if (gamePlayScreen != null) {
            gamePlayScreen.dispose();
        }
        if (miniGameHubScreen != null) {
            miniGameHubScreen.dispose();
        }
        if (comingSoonScreen != null) {
            comingSoonScreen.dispose();
        }
        if (scoreGameScreen != null) {
            scoreGameScreen.dispose();
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
                || active instanceof MarigoldLoadingScreen
                || transitioning
                || screen == active) {
            current = screen;
            transitioning = false;
            game.setScreen(screen);
            return;
        }

        float marigoldHold = marigoldHoldSeconds(screen, active);
        if (marigoldHold > 0f && !(active instanceof BootScreen)) {
            transitioning = true;
            current = screen;
            MarigoldLoadingScreen loading = new MarigoldLoadingScreen(
                    game, marigoldHold, screen, () -> transitioning = false);
            game.setScreen(loading);
            return;
        }

        TextureRegion snapshot = FadeTransitionScreen.captureFramebuffer();
        transitioning = true;
        current = screen;
        FadeTransitionScreen fade = new FadeTransitionScreen(game, snapshot, screen, () -> transitioning = false);
        game.setScreen(fade);
    }

    private static float marigoldHoldSeconds(Screen screen, Screen active) {
        if (screen instanceof PlantSelectionScreen) {
            return 2.5f;
        }
        if (screen instanceof GamePlayScreen && !(active instanceof PlantSelectionScreen)) {
            return 2.5f;
        }
        if (screen instanceof GreenhouseScreen || screen instanceof ScoreGameScreen) {
            return 1.5f;
        }
        return 0f;
    }
}
