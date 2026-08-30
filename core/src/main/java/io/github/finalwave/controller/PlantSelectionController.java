package io.github.finalwave.controller;

import io.github.finalwave.model.App;
import io.github.finalwave.model.adventure.ChapterConfig;
import io.github.finalwave.model.adventure.LevelConfig;
import io.github.finalwave.model.adventure.LevelType;
import io.github.finalwave.model.collection.CollectionPlantDetail;
import io.github.finalwave.model.collection.CollectionPlantEntry;
import io.github.finalwave.model.collection.CollectionPlantQuery;
import io.github.finalwave.model.collection.CollectionService;
import io.github.finalwave.model.collection.PlantCollection;
import io.github.finalwave.model.command.PlantSelectionMenuCommands;
import io.github.finalwave.model.definition.PlantRegistry;
import io.github.finalwave.model.definition.ZombieRegistry;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.entity.plant.PlantCategory;
import io.github.finalwave.model.game.mode.AdventureMode;
import io.github.finalwave.model.quest.QuestTracker;
import io.github.finalwave.model.user.User;
import io.github.finalwave.model.user.UserDatabase;
import io.github.finalwave.view.api.PlantSelectionView;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;

public class PlantSelectionController extends ViewController {

    protected final User user;
    protected final UserDatabase userDatabase;
    protected final ChapterConfig chapter;
    protected final LevelConfig level;
    protected final List<String> selected = new ArrayList<>();
    protected final Set<String> boosted = new LinkedHashSet<>();
    protected final PlantRegistry plantRegistry;
    protected final ZombieRegistry zombieRegistry;
    private final CollectionService collectionService;

    public PlantSelectionController(User user,
                                    UserDatabase userDatabase,
                                    ChapterConfig chapter,
                                    LevelConfig level) {
        this.user = user;
        this.userDatabase = userDatabase;
        this.chapter = chapter;
        this.level = level;
        this.plantRegistry = App.getInstance().getPlantRegistry();
        this.zombieRegistry = loadZombieRegistry();
        this.collectionService = CollectionService.createDefault(plantRegistry);
    }

    public User getUser() {
        return user;
    }

    public ChapterConfig getChapter() {
        return chapter;
    }

    public LevelConfig getLevel() {
        return level;
    }

    public PlantRegistry plantRegistry() {
        return plantRegistry;
    }

    public List<String> selectedPlants() {
        return List.copyOf(selected);
    }

    public Set<String> boostedPlants() {
        return Set.copyOf(boosted);
    }

    public boolean isBoosted(String name) {
        return name != null && (boosted.contains(name) || user.hasStoredBoost(name));
    }

    public boolean isOwned(String name) {
        return collectionService.canSelectPlant(user, name);
    }

    public boolean isRestricted(String name) {
        return level.getType() == LevelType.PLANT_WHAT_YOU_GET && isSunProducer(name);
    }

    public List<CollectionPlantEntry> plants(CollectionPlantQuery query) {
        return collectionService.listPlants(user, query);
    }

    public List<String> plantFamilies() {
        return collectionService.plantFamilies();
    }

    public CollectionPlantDetail plantDetail(String plantName) {
        if (!collectionService.isKnownPlant(plantName)) {
            getViewApi().errorPlantNotFound(plantName);
            return null;
        }
        return collectionService.plantDetail(user, plantName);
    }

    public void addPlant(String type) {
        handleAddPlant(type);
    }

    public void removePlant(String type) {
        handleRemovePlant(type);
    }

    public void boostPlant(String type) {
        handleBoostPlant(type);
    }

    public void startGame() {
        handleStartGame();
    }

    public void upgradePlant(String plantName) {
        PlantCollection.UpgradeResult result = collectionService.upgradePlant(user, plantName);
        if (result.success()) {
            userDatabase.saveUserWallet(user);
            userDatabase.savePlant(user, plantName);
            getViewApi().showPlantUpgraded(plantName, result.newLevel());
            return;
        }
        getViewApi().errorUpgradeFailed(upgradeFailureMessage(plantName, result.failure()));
    }

    public void back() {
        handleMenuExit();
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
        navigator.pop();
    }

    private void handleShowAllPlants() {
        List<String> names = plantRegistry.getAllDefinitions().stream()
                .map(def -> def.getName())
                .toList();
        getViewApi().showAllPlants(names);
    }

    protected void handleShowAvailablePlants() {
        List<String> available = collectionService.selectablePlantNames(user);
        if (level.getType() == LevelType.PLANT_WHAT_YOU_GET) {
            available = available.stream()
                    .filter(name -> !isSunProducer(name))
                    .toList();
        }
        getViewApi().showAvailablePlants(available);
    }

    protected void handleAddPlant(String type) {
        if (plantRegistry.getDefinition(type) == null) {
            getViewApi().errorPlantNotFound(type);
            return;
        }
        if (level.getType() == LevelType.PLANT_WHAT_YOU_GET && isSunProducer(type)) {
            getViewApi().errorSunProducerBanned(type);
            return;
        }
        if (!isOwned(type)) {
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

    private boolean isSunProducer(String plantName) {
        var definition = plantRegistry.getDefinition(plantName);
        return definition != null
                && PlantCategory.SUN_PRODUCER.name().equalsIgnoreCase(definition.getCategory());
    }

    private static String upgradeFailureMessage(String plantName, PlantCollection.UpgradeFailure failure) {
        if (failure == null) {
            return "Cannot upgrade " + plantName + ".";
        }
        return switch (failure) {
            case UNKNOWN_PLANT -> "Plant " + plantName + " does not exist.";
            case NOT_OWNED -> "Plant " + plantName + " is not owned.";
            case MAX_LEVEL -> plantName + " is already at max level.";
            case INSUFFICIENT_COINS -> "Not enough coins to upgrade " + plantName + ".";
            case INSUFFICIENT_SEED_PACKETS -> "Not enough seed packets to upgrade " + plantName + ".";
        };
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
        if (boosted.contains(type)) {
            return;
        }
        if (user.hasStoredBoost(type)) {
            user.getStoredBoosts().remove(type);
            userDatabase.saveStoredBoosts(user);
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

    protected void handleStartGame() {
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
        tracker.beginSession(session);
        session.attachQuestTracker(tracker);
        getViewApi().showGameStarted();
        GamePlayController gameplay = level.getType() == LevelType.NORMAL
                ? new GamePlayController(
                user, userDatabase, mode, session, chapter, level, boosted)
                : SpecialLevelControllerFactory.create(
                level.getType(), user, userDatabase, mode, session, chapter, level,
                boosted);
        navigator.replace(gameplay);
        session.start();
    }

    protected PlantSelectionView getViewApi() {
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
