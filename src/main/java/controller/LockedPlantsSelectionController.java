package controller;

import model.adventure.ChapterConfig;
import model.adventure.LevelConfig;
import model.game.GameSession;
import model.game.LockedPlantsRules;
import model.game.mode.AdventureMode;
import model.quest.QuestTracker;
import model.user.User;
import model.user.UserDatabase;
import view.api.LockedPlantsSelectionView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

public final class LockedPlantsSelectionController extends PlantSelectionController {

    private final LockedPlantsRules rules;

    public LockedPlantsSelectionController(User user,
                                           UserDatabase userDatabase,
                                           AdventureController adventureController,
                                           ChapterConfig chapter,
                                           LevelConfig level,
                                           LockedPlantsRules rules) {
        super(user, userDatabase, adventureController, chapter, level);
        this.rules = rules;
    }

    public LockedPlantsRules getRules() {
        return rules;
    }

    @Override
    public void displayMenu() {
        LockedPlantsSelectionView lockedView = getLockedPlantsView();
        lockedView.showLockedPlantsRules(rules.getMode());
        lockedView.showLockedPlants(new ArrayList<>(rules.getLockedPlants()));
        lockedView.showCurrentMenu();
        lockedView.showSelectedPlants(List.copyOf(selected));
    }

    @Override
    protected void handleShowAvailablePlants() {
        List<String> selectable = new ArrayList<>(rules.selectableFrom(
                user.getPlantProgress().getUnlockedPlantNames()));
        selectable.sort(String::compareToIgnoreCase);
        getLockedPlantsView().showAvailablePlants(selectable);
    }

    @Override
    protected void handleAddPlant(String type) {
        if (plantRegistry.getDefinition(type) == null) {
            getLockedPlantsView().errorPlantNotFound(type);
            return;
        }
        if (!user.getPlantProgress().isOwned(type)) {
            getLockedPlantsView().errorPlantLocked(type);
            return;
        }
        if (rules.isLocked(type)) {
            getLockedPlantsView().errorPlantLockedForLevel(type);
            return;
        }
        if (selected.contains(type)) {
            getLockedPlantsView().errorPlantAlreadySelected(type);
            return;
        }
        if (selected.size() >= level.getPlantSlotCount()) {
            getLockedPlantsView().errorLoadoutFull(level.getPlantSlotCount());
            return;
        }
        selected.add(type);
        getLockedPlantsView().showPlantAdded(type);
    }

    @Override
    protected void handleStartGame() {
        if (selected.isEmpty()) {
            getLockedPlantsView().errorLoadoutEmpty();
            return;
        }
        AdventureMode mode = new AdventureMode(
                chapter, level, plantRegistry, zombieRegistry,
                user.getDifficultyLevel(), new Random());
        GameSession session = mode.createSession();
        session.setSelectedLoadout(Set.copyOf(selected));
        session.activateLockedPlants(rules);
        QuestTracker tracker = user.ensureQuestTracker();
        tracker.registerOn(session.getEventBus());
        tracker.beginSession(session);
        session.attachQuestTracker(tracker);
        getLockedPlantsView().showGameStarted();
        LockedPlantsLevelController gameplay = new LockedPlantsLevelController(
                user, userDatabase, adventureController, mode, session, chapter, level, boosted, rules);
        parser.switchController(gameplay);
        session.start();
    }

    private LockedPlantsSelectionView getLockedPlantsView() {
        return (LockedPlantsSelectionView) view;
    }
}
