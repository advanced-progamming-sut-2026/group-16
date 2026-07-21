package controller;

import model.App;
import model.adventure.ChapterConfig;
import model.adventure.LevelConfig;
import model.adventure.LevelType;
import model.command.PlantSelectionMenuCommands;
import model.definition.PlantRegistry;
import model.definition.ZombieRegistry;
import model.game.GameSession;
import model.game.mode.AdventureMode;
import model.quest.QuestTracker;
import model.user.User;
import model.user.UserDatabase;
import view.api.PlantSelectionView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;

public class PlantSelectionController extends ViewController {

    private final User user;
    private final UserDatabase userDatabase;
    private final AdventureController adventureController;
    private final ChapterConfig chapter;
    private final LevelConfig level;
    private final List<String> selected = new ArrayList<>();
    private final Set<String> boosted = new LinkedHashSet<>();
    private final PlantRegistry plantRegistry;
    private final ZombieRegistry zombieRegistry;

    public PlantSelectionController(User user,
                                    UserDatabase userDatabase,
                                    AdventureController adventureController,
                                    ChapterConfig chapter,
                                    LevelConfig level) {
        this.user = user;
        this.userDatabase = userDatabase;
        this.adventureController = adventureController;
        this.chapter = chapter;
        this.level = level;
        this.plantRegistry = App.getInstance().getPlantRegistry();
        this.zombieRegistry = loadZombieRegistry();
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
                case MENU_SHOW_CURRENT -> handleShowCurrent();
                case MENU_EXIT -> handleMenuExit();
                case SHOW_ALL_PLANTS -> handleShowAllPlants();
                case SHOW_AVAILABLE_PLANTS -> handleShowAvailablePlants();
                case ADD_PLANT -> handleAddPlant(matcher.group("type").trim());
                case REMOVE_PLANT -> handleRemovePlant(matcher.group("type").trim());
                case BOOST_PLANT -> handleBoostPlant(matcher.group("type").trim());
                case START_GAME -> handleStartGame();
            }
            return;
        }
        getViewApi().errorInvalidCommand();
    }

    private void handleShowCurrent() {
        getViewApi().showCurrentMenu();
        getViewApi().showSelectedPlants(List.copyOf(selected));
    }

    private void handleMenuExit() {
        parser.switchController(adventureController);
    }

    private void handleShowAllPlants() {
        List<String> names = plantRegistry.getAllDefinitions().stream()
                .map(def -> def.getName())
                .toList();
        getViewApi().showAllPlants(names);
    }

    private void handleShowAvailablePlants() {
        getViewApi().showAvailablePlants(user.getPlantProgress().getUnlockedPlantNames());
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
        if (selected.size() >= level.getPlantSlotCount()) {
            getViewApi().errorLoadoutFull(level.getPlantSlotCount());
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
        AdventureMode mode = new AdventureMode(
                chapter, level, plantRegistry, zombieRegistry,
                user.getDifficultyLevel(), new Random());
        GameSession session = mode.createSession();
        session.setSelectedLoadout(Set.copyOf(selected));
        QuestTracker tracker = user.ensureQuestTracker();
        tracker.registerOn(session.getEventBus());
        tracker.beginSession();
        session.attachQuestTracker(tracker);
        getViewApi().showGameStarted();
        GamePlayController gameplay = level.getType() == LevelType.NORMAL
                ? new GamePlayController(
                        user, userDatabase, adventureController, mode, session, chapter, level, boosted)
                : SpecialLevelControllerFactory.create(
                        level.getType(), user, userDatabase, adventureController, mode, session, chapter, level,
                        boosted);
        parser.switchController(gameplay);
        session.start();
    }

    private PlantSelectionView getViewApi() {
        return (PlantSelectionView) view;
    }

    static ZombieRegistry loadZombieRegistry() {
        ZombieRegistry registry = new ZombieRegistry();
        try (InputStream zombies = PlantSelectionController.class.getClassLoader()
                .getResourceAsStream("zombies.json");
             InputStream armor = PlantSelectionController.class.getClassLoader()
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
