package io.github.finalwave.controller;

import io.github.finalwave.model.App;
import io.github.finalwave.model.command.MiniGameMenuCommands;
import io.github.finalwave.model.definition.PlantRegistry;
import io.github.finalwave.model.definition.ZombieRegistry;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.minigame.MiniGameId;
import io.github.finalwave.model.minigame.MiniGameRegistry;
import io.github.finalwave.model.minigame.MiniGameStageConfig;
import io.github.finalwave.model.minigame.mode.BeghouledMode;
import io.github.finalwave.model.minigame.mode.IZombieMode;
import io.github.finalwave.model.minigame.mode.NetworkedIZombieMode;
import io.github.finalwave.model.minigame.mode.VaseBreakerMode;
import io.github.finalwave.model.minigame.mode.WalnutBowlingMode;
import io.github.finalwave.model.user.User;
import io.github.finalwave.model.user.UserDatabase;
import io.github.finalwave.controller.Navigator;
import io.github.finalwave.network.match.MatchRole;
import io.github.finalwave.network.match.MatchStartPayload;
import io.github.finalwave.network.match.MatchmakingService;
import io.github.finalwave.network.match.MatchSyncService;
import io.github.finalwave.network.match.NetworkMatchServices;
import io.github.finalwave.view.api.minigame.MiniGameHubView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;

public class MiniGameHubController extends ViewController {

    private final User user;
    private final UserDatabase userDatabase;
    private final MiniGameId preselected;
    private MiniGameId selectedGame;

    public MiniGameHubController(User user,
                                 UserDatabase userDatabase) {
        this(user, userDatabase, null);
    }

    public MiniGameHubController(User user,
                                 UserDatabase userDatabase,
                                 MiniGameId preselected) {
        this.user = user;
        this.userDatabase = userDatabase;
        this.preselected = preselected;
        if (preselected != null && minigameUnlocked(preselected)) {
            this.selectedGame = preselected;
        }
    }

    @Override
    public void displayMenu() {
        if (selectedGame != null) {
            getHubView().showEnteredGame(selectedGame);
            handleShowStages();
            return;
        }
        getHubView().showCurrentMenu();
        handleShowGames();
    }

    @Override
    public void handleCommand(String input) {
        for (MiniGameMenuCommands cmd : MiniGameMenuCommands.values()) {
            Matcher matcher = cmd.getMatcher(input);
            if (matcher == null) {
                continue;
            }
            switch (cmd) {
                case MENU_SHOW_CURRENT -> getHubView().showCurrentMenu();
                case MENU_EXIT -> handleMenuExit();
                case SHOW_GAMES -> handleShowGames();
                case ENTER_GAME -> handleEnterGame(matcher.group("name"));
                case SHOW_STAGES -> handleShowStages();
                case START_STAGE -> handleStartStage(matcher.group("stage"));
            }
            return;
        }
        getHubView().errorInvalidCommand();
    }

    private void handleMenuExit() {
        if (selectedGame != null && preselected == null) {
            selectedGame = null;
            getHubView().showCurrentMenu();
            handleShowGames();
            return;
        }
        navigator.pop();
    }

    private void handleShowGames() {
        List<String> lines = new ArrayList<>();
        for (MiniGameId id : MiniGameRegistry.getInstance().getAllMiniGames()) {
            boolean unlocked = minigameUnlocked(id);
            List<MiniGameStageConfig> stages = MiniGameRegistry.getInstance().getStages(id);
            boolean implemented = stages.stream().anyMatch(MiniGameStageConfig::isImplemented);
            String status = !unlocked ? "LOCKED"
                    : !implemented ? "COMING SOON"
                    : "UNLOCKED";
            lines.add(id.getKey() + " | " + id.getDisplayName() + " | " + status);
        }
        getHubView().showGames(lines);
    }

    private void handleEnterGame(String name) {
        MiniGameId id = MiniGameId.fromName(name);
        if (id == null) {
            getHubView().errorUnknownGame(name);
            return;
        }
        if (!minigameUnlocked(id)) {
            getHubView().errorGameLocked(id.getDisplayName());
            return;
        }
        List<MiniGameStageConfig> stages = MiniGameRegistry.getInstance().getStages(id);
        boolean implemented = stages.stream().anyMatch(MiniGameStageConfig::isImplemented);
        if (!implemented) {
            getHubView().showComingSoon(id);
            return;
        }
        selectedGame = id;
        getHubView().showEnteredGame(id);
        handleShowStages();
    }

    private void handleShowStages() {
        if (selectedGame == null) {
            getHubView().errorNoGameSelected();
            return;
        }
        List<String> lines = new ArrayList<>();
        for (StageInfo stage : selectedStages()) {
            String status = stage.completed() ? "DONE"
                    : stage.playable() ? "OPEN"
                    : "LOCKED";
            lines.add("Stage " + stage.index() + " | " + stage.detail() + " | " + status);
        }
        getHubView().showStages(selectedGame, lines);
    }

    private void handleStartStage(String stageText) {
        if (selectedGame == null) {
            getHubView().errorNoGameSelected();
            return;
        }
        int stageIndex;
        try {
            stageIndex = Integer.parseInt(stageText.trim());
        } catch (NumberFormatException e) {
            getHubView().errorInvalidStage();
            return;
        }
        MiniGameStageConfig stage = MiniGameRegistry.getInstance().getStage(selectedGame, stageIndex);
        if (stage == null) {
            getHubView().errorStageNotFound(stageIndex);
            return;
        }
        List<MiniGameStageConfig> stages = MiniGameRegistry.getInstance().getStages(selectedGame);
        if (!stageOpen(selectedGame, stageIndex, stages.size())) {
            getHubView().errorStageLocked(stageIndex);
            return;
        }
        if (!stage.isImplemented()) {
            getHubView().showComingSoon(selectedGame);
            return;
        }
        switch (selectedGame) {
            case VASE_BREAKER -> startVaseBreaker(stage);
            case WALNUT_BOWLING -> startWalnutBowling(stage);
            case I_ZOMBIE -> startIZombie(stage);
            case BEGHOULED -> startBeghouled(stage);
            case ZOMBOTANY -> startZombotanySelect(stage);
            default -> getHubView().showComingSoon(selectedGame);
        }
    }

    private void startZombotanySelect(MiniGameStageConfig stage) {
        navigator.push(new ZombotanyPlantSelectionController(user, userDatabase, stage));
    }

    private void startBeghouled(MiniGameStageConfig stage) {
        PlantRegistry plantRegistry = App.getInstance().getPlantRegistry();
        ZombieRegistry zombieRegistry = loadZombieRegistry();
        BeghouledMode mode = new BeghouledMode(stage, plantRegistry, zombieRegistry, new Random());
        GameSession session = mode.createSession();
        BeghouledController controller = new BeghouledController(
                user, userDatabase, mode, session, stage);
        navigator.push(controller);
        session.start();
    }

    private void startIZombie(MiniGameStageConfig stage) {
        if (NetworkMatchServices.isOnlineCapable()) {
            MatchmakingService matchmaking = NetworkMatchServices.matchmaking();
            matchmaking.setListener(null);
            IZombieMatchmakingController matchmakingController = new IZombieMatchmakingController(
                    user,
                    matchmaking,
                    NetworkMatchServices.userStatus(),
                    NetworkMatchServices.directory(),
                    NetworkMatchServices.matchSync(),
                    stage);
            navigator.push(matchmakingController);
            matchmaking.setListener(matchmakingController);
            return;
        }
        startSinglePlayerIZombie(stage);
    }

    public static void launchNetworkedIZombieMatch(
            Navigator navigator,
            User user,
            MatchStartPayload start,
            MatchSyncService matchSyncService,
            MiniGameStageConfig stage) {
        if (start == null || start.getMatchId() == null || start.getMatchId().isBlank()) {
            return;
        }
        String activeMatchId = matchSyncService.matchId();
        if (activeMatchId != null && activeMatchId.equals(start.getMatchId())) {
            return;
        }
        MiniGameStageConfig resolvedStage = MiniGameStageConfig.iZombieNetwork();
        PlantRegistry plantRegistry = App.getInstance().getPlantRegistry();
        ZombieRegistry zombieRegistry = loadZombieRegistry();
        NetworkedIZombieMode mode = new NetworkedIZombieMode(resolvedStage, plantRegistry, zombieRegistry, new Random());
        GameSession session = start.getYourRole() == MatchRole.PLANT
                ? mode.createHostSession()
                : mode.createGuestSession();
        session.start();
        NetworkedIZombieController controller = new NetworkedIZombieController(
                user,
                mode,
                session,
                resolvedStage,
                start.getYourRole(),
                matchSyncService,
                start.getOpponentUsername(),
                start);
        matchSyncService.registerMatch(start, session);
        matchSyncService.setListener(controller::handleNetworkMatchEnd);
        NetworkMatchServices.matchmaking().setListener(null);
        navigator.push(controller);
    }

    private void startSinglePlayerIZombie(MiniGameStageConfig stage) {
        PlantRegistry plantRegistry = App.getInstance().getPlantRegistry();
        ZombieRegistry zombieRegistry = loadZombieRegistry();
        IZombieMode mode = new IZombieMode(stage, plantRegistry, zombieRegistry, new Random());
        GameSession session = mode.createSession();
        IZombieController controller = new IZombieController(
                user, userDatabase, mode, session, stage);
        navigator.push(controller);
        session.start();
    }

    private void startWalnutBowling(MiniGameStageConfig stage) {
        PlantRegistry plantRegistry = App.getInstance().getPlantRegistry();
        ZombieRegistry zombieRegistry = loadZombieRegistry();
        WalnutBowlingMode mode = new WalnutBowlingMode(stage, plantRegistry, zombieRegistry, new Random());
        GameSession session = mode.createSession();
        WalnutBowlingController controller = new WalnutBowlingController(
                user, userDatabase, mode, session, stage);
        navigator.push(controller);
        session.start();
    }

    private void startVaseBreaker(MiniGameStageConfig stage) {
        PlantRegistry plantRegistry = App.getInstance().getPlantRegistry();
        ZombieRegistry zombieRegistry = loadZombieRegistry();
        VaseBreakerMode mode = new VaseBreakerMode(stage, plantRegistry, zombieRegistry, new Random());
        GameSession session = mode.createSession();
        VaseBreakerController controller = new VaseBreakerController(
                user, userDatabase, mode, session, stage);
        navigator.push(controller);
        session.start();
    }

    static ZombieRegistry loadZombieRegistry() {
        return PlantSelectionController.loadZombieRegistry();
    }

    static ZombieRegistry loadZombotanyZombieRegistry() {
        ZombieRegistry registry = loadZombieRegistry();
        try (InputStream extra = MiniGameHubController.class.getClassLoader()
                .getResourceAsStream("zombotany-zombies.json")) {
            if (extra == null) {
                throw new IllegalStateException("zombotany-zombies.json is missing from application resources");
            }
            registry.loadFromJson(extra);
        } catch (IOException e) {
            throw new IllegalStateException("Could not load zombotany zombie registry", e);
        }
        return registry;
    }

    private MiniGameHubView getHubView() {
        return (MiniGameHubView) view;
    }

    public void back() {
        handleMenuExit();
    }

    public void startStage(int stageIndex) {
        handleStartStage(String.valueOf(stageIndex));
    }

    public MiniGameId selectedGame() {
        return selectedGame;
    }

    public List<StageInfo> selectedStages() {
        if (selectedGame == null) {
            return List.of();
        }
        List<MiniGameStageConfig> stages = MiniGameRegistry.getInstance().getStages(selectedGame);
        int maxStage = stages.size();
        List<StageInfo> rows = new ArrayList<>();
        for (MiniGameStageConfig stage : stages) {
            boolean completed = user.getMiniGameProgress()
                    .isStageCompleted(selectedGame, stage.getStageIndex());
            boolean open = stageOpen(selectedGame, stage.getStageIndex(), maxStage);
            rows.add(new StageInfo(
                    stage.getStageIndex(),
                    stageDetail(stage),
                    completed,
                    open,
                    stage.isImplemented()));
        }
        return rows;
    }

    private boolean minigameUnlocked(MiniGameId id) {
        return user.isDebugMode() || user.getUnlockedMinigames().contains(id.getKey());
    }

    private boolean stageOpen(MiniGameId id, int stageIndex, int maxStage) {
        return user.isDebugMode() || user.getMiniGameProgress().isStagePlayable(id, stageIndex, maxStage);
    }

    private String stageDetail(MiniGameStageConfig stage) {
        if (selectedGame == MiniGameId.WALNUT_BOWLING) {
            return "waves=" + stage.getWaveCount() + " redLine=" + stage.getRedLineColumn();
        }
        if (selectedGame == MiniGameId.I_ZOMBIE) {
            return "sun=" + stage.getStartingSun() + " redLine=" + stage.getRedLineColumn();
        }
        if (selectedGame == MiniGameId.BEGHOULED) {
            return "matchTarget=" + stage.getMatchTarget();
        }
        if (selectedGame == MiniGameId.ZOMBOTANY) {
            return "waves=" + stage.getWaveCount() + " sun=" + stage.getStartingSun();
        }
        return "pots=" + stage.getPotCount();
    }

    public record StageInfo(
            int index,
            String detail,
            boolean completed,
            boolean playable,
            boolean implemented) {
        public boolean locked() {
            return !playable;
        }
    }
}
