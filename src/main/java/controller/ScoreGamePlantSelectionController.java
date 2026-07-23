package controller;

import model.App;
import model.command.PlantSelectionMenuCommands;
import model.definition.PlantRegistry;
import model.definition.ZombieRegistry;
import model.game.GameSession;
import model.scoregame.MeowPointTracker;
import model.scoregame.ScoreGameSessionFactory;
import model.user.User;
import model.user.UserDatabase;
import view.api.PlantSelectionView;

import java.io.IOException;
import java.io.InputStream;
import java.time.Clock;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;

public class ScoreGamePlantSelectionController extends ViewController {
    private final User user;
    private final UserDatabase userDatabase;
    private final ScoreGameController scoreGameController;
    private final List<String> selected = new ArrayList<>();
    private final Set<String> boosted = new LinkedHashSet<>();
    private final PlantRegistry plantRegistry;
    private final ZombieRegistry zombieRegistry;
    private final Clock clock;

    public ScoreGamePlantSelectionController(User user,
                                             UserDatabase userDatabase,
                                             ScoreGameController scoreGameController) {
        this(user, userDatabase, scoreGameController, Clock.systemUTC());
    }

    ScoreGamePlantSelectionController(User user,
                                      UserDatabase userDatabase,
                                      ScoreGameController scoreGameController,
                                      Clock clock) {
        this.user = user;
        this.userDatabase = userDatabase;
        this.scoreGameController = scoreGameController;
        this.plantRegistry = App.getInstance().getPlantRegistry();
        this.zombieRegistry = loadZombieRegistry();
        this.clock = clock;
    }

    @Override
    public void displayMenu() {
        getViewApi().showCurrentMenu();
        getViewApi().showSelectedPlants(List.copyOf(selected));
    }

    @Override
    public void handleCommand(String input) {
        for (PlantSelectionMenuCommands cmd : PlantSelectionMenuCommands.values()) {
            Matcher matcher = cmd.getMatcher(input);
            if (matcher == null) {
                continue;
            }
            switch (cmd) {
                case MENU_SHOW_CURRENT -> {
                    getViewApi().showCurrentMenu();
                    getViewApi().showSelectedPlants(List.copyOf(selected));
                }
                case MENU_EXIT -> parser.switchController(scoreGameController);
                case SHOW_ALL_PLANTS -> getViewApi().showAllPlants(
                        plantRegistry.getAllDefinitions().stream()
                                .map(def -> def.getName())
                                .toList());
                case SHOW_AVAILABLE_PLANTS -> getViewApi().showAvailablePlants(
                        user.getPlantProgress().getUnlockedPlantNames());
                case ADD_PLANT -> handleAddPlant(matcher.group("type").trim());
                case REMOVE_PLANT -> handleRemovePlant(matcher.group("type").trim());
                case BOOST_PLANT -> handleBoostPlant(matcher.group("type").trim());
                case START_GAME -> handleStartGame();
            }
            return;
        }
        getViewApi().errorInvalidCommand();
    }

    private void handleAddPlant(String type) {
        if (plantRegistry.getDefinition(type) == null) {
            getViewApi().errorPlantNotFound(type);
            return;
        }
        if (!user.getPlantProgress().isOwned(type)) {
            getViewApi().errorPlantLocked(type);
            return;
        }
        if (selected.contains(type)) {
            getViewApi().errorPlantAlreadySelected(type);
            return;
        }
        if (selected.size() >= 8) {
            getViewApi().errorLoadoutFull(8);
            return;
        }
        selected.add(type);
        getViewApi().showPlantAdded(type);
    }

    private void handleRemovePlant(String type) {
        if (!selected.remove(type)) {
            getViewApi().errorPlantNotSelected(type);
            return;
        }
        boosted.remove(type);
        getViewApi().showPlantRemoved(type);
    }

    private void handleBoostPlant(String type) {
        if (!selected.contains(type)) {
            getViewApi().errorCannotBoostPlant(type);
            return;
        }
        if (user.hasStoredBoost(type)) {
            user.getStoredBoosts().remove(type);
            userDatabase.saveUserWallet(user);
            boosted.add(type);
            getViewApi().showPlantBoosted(type);
            return;
        }
        if (!user.spendDiamonds(2)) {
            getViewApi().errorNotEnoughDiamonds();
            return;
        }
        userDatabase.saveUserWallet(user);
        boosted.add(type);
        getViewApi().showPlantBoosted(type);
    }

    private void handleStartGame() {
        if (selected.isEmpty()) {
            getViewApi().errorLoadoutEmpty();
            return;
        }
        var match = ScoreGameSessionFactory.create(plantRegistry, zombieRegistry, clock);
        GameSession session = match.session();
        session.setSelectedLoadout(Set.copyOf(selected));
        MeowPointTracker tracker = match.tracker();
        getViewApi().showGameStarted();
        ScoreGamePlayController gameplay = new ScoreGamePlayController(
                user, userDatabase, scoreGameController, match.mode(), session,
                match.chapter(), match.level(), boosted, tracker);
        parser.switchController(gameplay);
        session.start();
    }

    private PlantSelectionView getViewApi() {
        return (PlantSelectionView) view;
    }

    private static ZombieRegistry loadZombieRegistry() {
        ZombieRegistry registry = new ZombieRegistry();
        try (InputStream zombies = ScoreGamePlantSelectionController.class.getClassLoader()
                .getResourceAsStream("zombies.json");
             InputStream armor = ScoreGamePlantSelectionController.class.getClassLoader()
                     .getResourceAsStream("ArmorTypeData.json")) {
            if (zombies == null || armor == null) {
                throw new IllegalStateException("zombie resources missing");
            }
            registry.loadFromJson(zombies);
            registry.loadArmorFromJson(armor);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load zombie registry", e);
        }
        return registry;
    }
}
