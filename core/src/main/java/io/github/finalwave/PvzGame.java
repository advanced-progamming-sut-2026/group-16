package io.github.finalwave;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import io.github.finalwave.controller.AppBootstrap;
import io.github.finalwave.model.App;
import io.github.finalwave.network.NetworkManager;
import io.github.finalwave.network.NetworkPingProbe;
import io.github.finalwave.login.NetworkLoginGateway;
import io.github.finalwave.login.NetworkPasswordChangeGateway;
import io.github.finalwave.leaderboard.NetworkLeaderboardGateway;
import io.github.finalwave.score.NetworkScoreSubmitGateway;
import io.github.finalwave.network.sync.ProgressSyncService;
import io.github.finalwave.network.match.MatchLaunchBridge;
import io.github.finalwave.network.match.MatchDirectoryService;
import io.github.finalwave.network.match.MatchmakingService;
import io.github.finalwave.network.match.MatchSyncService;
import io.github.finalwave.network.match.NetworkMatchServices;
import io.github.finalwave.network.match.UserStatusService;
import io.github.finalwave.registration.NetworkRegistrationGateway;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.bind.GuiNavigationBinder;
import io.github.finalwave.view.gui.screen.BootScreen;
import io.github.finalwave.view.gui.screen.ScreenRouter;
import io.github.finalwave.model.user.UserDatabase;


public final class PvzGame extends Game {
    private static final String NETWORK_HOST = "127.0.0.1";
    private static final int NETWORK_PORT = 5454;

    private GameAssets assets;
    private ScreenRouter router;
    private AppBootstrap bootstrap;
    private BootScreen bootScreen;
    private NetworkManager networkManager;
    private ProgressSyncService progressSyncService;
    private MatchmakingService matchmakingService;
    private MatchSyncService matchSyncService;
    private UserStatusService userStatusService;
    private MatchDirectoryService matchDirectoryService;
    private boolean applicationStarted;

    @Override
    public void create() {
        assets = new GameAssets(Gdx.files.local("."));
        router = new ScreenRouter(this);
        GuiNavigationBinder binder = new GuiNavigationBinder(router);
        networkManager = new NetworkManager();
        progressSyncService = new ProgressSyncService(
                networkManager,
                UserDatabase.getInstance(),
                NETWORK_HOST,
                NETWORK_PORT
        );
        matchmakingService = new MatchmakingService(networkManager);
        matchSyncService = new MatchSyncService(networkManager);
        userStatusService = new UserStatusService(networkManager);
        matchDirectoryService = new MatchDirectoryService(networkManager);
        NetworkMatchServices.install(
                networkManager,
                matchmakingService,
                matchSyncService,
                userStatusService,
                matchDirectoryService);
        UserDatabase.getInstance().addWriteListener(progressSyncService);
        networkManager.addConnectionListener(progressSyncService);
        new NetworkPingProbe(networkManager).start(NETWORK_HOST, NETWORK_PORT);
        NetworkLoginGateway loginGateway = new NetworkLoginGateway(networkManager, progressSyncService);
        new NetworkPasswordChangeGateway(networkManager);
        NetworkLeaderboardGateway leaderboardGateway = new NetworkLeaderboardGateway(networkManager, progressSyncService);
        NetworkScoreSubmitGateway scoreSubmitGateway = new NetworkScoreSubmitGateway(networkManager, progressSyncService);
        bootstrap = new AppBootstrap(
                UserDatabase.getInstance(),
                new NetworkRegistrationGateway(networkManager, progressSyncService),
                loginGateway,
                leaderboardGateway,
                scoreSubmitGateway,
                binder,
                true
        );
        bootScreen = new BootScreen(this);
        setScreen(bootScreen);
    }


    public void startApplication() {
        if (applicationStarted) {
            return;
        }
        applicationStarted = true;
        MatchLaunchBridge.install(
                matchmakingService,
                matchSyncService,
                bootstrap::navigator,
                () -> App.getInstance().getCurrentUser());
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

    public NetworkManager networkManager() {
        return networkManager;
    }


    public void installScreen(Screen screen) {
        this.screen = screen;
    }

    @Override
    public void render() {
        if (networkManager != null) {
            networkManager.drainIncoming();
        }
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
        if (networkManager != null) {
            networkManager.disconnect();
        }
        if (progressSyncService != null) {
            progressSyncService.disarm();
        }
        super.dispose();
    }
}
