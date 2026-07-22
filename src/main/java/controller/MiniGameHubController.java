package controller;

import model.App;
import model.command.MiniGameMenuCommands;
import model.definition.PlantRegistry;
import model.definition.ZombieRegistry;
import model.game.GameSession;
import model.minigame.MiniGameId;
import model.minigame.MiniGameRegistry;
import model.minigame.MiniGameStageConfig;
import model.minigame.mode.IZombieMode;
import model.minigame.mode.VaseBreakerMode;
import model.minigame.mode.WalnutBowlingMode;
import model.user.User;
import model.user.UserDatabase;
import view.api.minigame.MiniGameHubView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;

public class MiniGameHubController extends ViewController {

    private final User user;
    private final UserDatabase userDatabase;
    private final TravelLogController travelLogController;
    private MiniGameId selectedGame;

    public MiniGameHubController(User user,
                                 UserDatabase userDatabase,
                                 TravelLogController travelLogController) {
        this.user = user;
        this.userDatabase = userDatabase;
        this.travelLogController = travelLogController;
    }

    @Override
    public void displayMenu() {
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
        if (selectedGame != null) {
            selectedGame = null;
            getHubView().showCurrentMenu();
            handleShowGames();
            return;
        }
        parser.switchController(travelLogController);
    }

    private void handleShowGames() {
        List<String> lines = new ArrayList<>();
        for (MiniGameId id : MiniGameRegistry.getInstance().getAllMiniGames()) {
            boolean unlocked = user.getUnlockedMinigames().contains(id.getKey());
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
        if (!user.getUnlockedMinigames().contains(id.getKey())) {
            getHubView().errorGameLocked(id.getDisplayName());
            return;
        }
        List<MiniGameStageConfig> stages = MiniGameRegistry.getInstance().getStages(id);
        boolean implemented = stages.stream().anyMatch(MiniGameStageConfig::isImplemented);
        if (!implemented) {
            switch (id) {
                case BEGHOULED -> parser.switchController(new BeghouledController(user, this));
                case ZOMBOTANY -> parser.switchController(new ZombotanyController(user, this));
                default -> getHubView().showComingSoon(id);
            }
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
        List<MiniGameStageConfig> stages = MiniGameRegistry.getInstance().getStages(selectedGame);
        int maxStage = stages.size();
        int playable = user.getMiniGameProgress().highestPlayableStage(selectedGame, maxStage);
        List<String> lines = new ArrayList<>();
        for (MiniGameStageConfig stage : stages) {
            boolean completed = user.getMiniGameProgress()
                    .isStageCompleted(selectedGame, stage.getStageIndex());
            String status = completed ? "DONE"
                    : stage.getStageIndex() <= playable ? "OPEN"
                    : "LOCKED";
            String stageInfo;
            if (selectedGame == MiniGameId.WALNUT_BOWLING) {
                stageInfo = "waves=" + stage.getWaveCount() + " redLine=" + stage.getRedLineColumn();
            } else if (selectedGame == MiniGameId.I_ZOMBIE) {
                stageInfo = "sun=" + stage.getStartingSun() + " redLine=" + stage.getRedLineColumn();
            } else {
                stageInfo = "pots=" + stage.getPotCount();
            }
            lines.add("Stage " + stage.getStageIndex() + " | " + stageInfo + " | " + status);
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
        if (!user.getMiniGameProgress().isStagePlayable(selectedGame, stageIndex, stages.size())) {
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
            default -> getHubView().showComingSoon(selectedGame);
        }
    }

    private void startIZombie(MiniGameStageConfig stage) {
        PlantRegistry plantRegistry = App.getInstance().getPlantRegistry();
        ZombieRegistry zombieRegistry = loadZombieRegistry();
        IZombieMode mode = new IZombieMode(stage, plantRegistry, zombieRegistry, new Random());
        GameSession session = mode.createSession();
        IZombieController controller = new IZombieController(
                user, userDatabase, this, mode, session, stage);
        parser.switchController(controller);
        session.start();
    }

    private void startWalnutBowling(MiniGameStageConfig stage) {
        PlantRegistry plantRegistry = App.getInstance().getPlantRegistry();
        ZombieRegistry zombieRegistry = loadZombieRegistry();
        WalnutBowlingMode mode = new WalnutBowlingMode(stage, plantRegistry, zombieRegistry, new Random());
        GameSession session = mode.createSession();
        WalnutBowlingController controller = new WalnutBowlingController(
                user, userDatabase, this, mode, session, stage);
        parser.switchController(controller);
        session.start();
    }

    private void startVaseBreaker(MiniGameStageConfig stage) {
        PlantRegistry plantRegistry = App.getInstance().getPlantRegistry();
        ZombieRegistry zombieRegistry = loadZombieRegistry();
        VaseBreakerMode mode = new VaseBreakerMode(stage, plantRegistry, zombieRegistry, new Random());
        GameSession session = mode.createSession();
        VaseBreakerController controller = new VaseBreakerController(
                user, userDatabase, this, mode, session, stage);
        parser.switchController(controller);
        session.start();
    }

    static ZombieRegistry loadZombieRegistry() {
        try {
            ZombieRegistry registry = new ZombieRegistry();
            registry.loadFromJson("src/main/resources/zombies.json");
            registry.loadArmorFromJson("src/main/resources/ArmorTypeData.json");
            return registry;
        } catch (IOException e) {
            throw new RuntimeException("Could not load zombie registry", e);
        }
    }

    private MiniGameHubView getHubView() {
        return (MiniGameHubView) view;
    }
}
