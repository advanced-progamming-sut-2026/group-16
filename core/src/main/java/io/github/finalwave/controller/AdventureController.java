package io.github.finalwave.controller;

import io.github.finalwave.model.App;
import io.github.finalwave.model.adventure.ChapterConfig;
import io.github.finalwave.model.adventure.LevelConfig;
import io.github.finalwave.model.adventure.LevelType;
import io.github.finalwave.model.collection.CollectionService;
import io.github.finalwave.model.command.AdventureMenuCommands;
import io.github.finalwave.model.definition.PlantRegistry;
import io.github.finalwave.model.definition.ZombieRegistry;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.LockedPlantsRules;
import io.github.finalwave.model.game.LockedPlantsRulesFactory;
import io.github.finalwave.model.game.mode.AdventureMode;
import io.github.finalwave.model.quest.QuestTracker;
import io.github.finalwave.model.user.ChapterProgress;
import io.github.finalwave.model.user.User;
import io.github.finalwave.model.user.UserDatabase;
import io.github.finalwave.view.api.AdventureView;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;

public class AdventureController extends ViewController {

    private final User user;
    private final UserDatabase userDatabase;
    private final ChapterConfig chapter;

    public AdventureController(User user,
                               UserDatabase userDatabase,
                               ChapterConfig chapter) {
        this.user = user;
        this.userDatabase = userDatabase;
        this.chapter = chapter;
    }

    public ChapterConfig getChapter() {
        return chapter;
    }

    public User getUser() {
        return user;
    }

    public void back() {
        navigator.pop();
    }

    public void openGreenhouse() {
        navigator.push(new GreenhouseController(user, userDatabase));
    }

    public void openSettings() {
        navigator.push(new SettingController(user, userDatabase));
    }

    public void openCollection() {
        navigator.push(new CollectionController(user, userDatabase));
    }

    public void startLevel(int levelIndex) {
        handleStartLevel(String.valueOf(levelIndex));
    }

    public boolean isLevelUnlocked(int levelIndex) {
        if (user.isDebugMode()) {
            return true;
        }
        ChapterProgress progress = user.getChapterProgress();
        if (!progress.isChapterUnlocked(chapter.getId())) {
            return false;
        }
        if (levelIndex <= 1) {
            return true;
        }
        return progress.isLevelCompleted(chapter.getId(), levelIndex - 1);
    }

    @Override
    public void displayMenu() {
        getAdventureView().showAdventureMenu(chapter);
        getAdventureView().showCurrentMenu(chapter.getDisplayName());
    }

    @Override
    public void handleCommand(String input) {
        for (AdventureMenuCommands cmd : AdventureMenuCommands.values()) {
            Matcher matcher = cmd.getMatcher(input);
            if (matcher == null) {
                continue;
            }
            switch (cmd) {
                case MENU_SHOW_CURRENT -> getAdventureView().showCurrentMenu(chapter.getDisplayName());
                case MENU_EXIT -> navigator.pop();
                case SHOW_LEVELS -> getAdventureView().showLevels(chapter, chapter.getLevels());
                case START_LEVEL -> handleStartLevel(matcher.group("level"));
                case SHOW_PROGRESS -> handleShowProgress();
            }
            return;
        }
        getAdventureView().errorInvalidCommand();
    }

    private void handleStartLevel(String levelText) {
        int levelIndex;
        try {
            levelIndex = Integer.parseInt(levelText.trim());
        } catch (NumberFormatException e) {
            getAdventureView().errorInvalidLevelNumber();
            return;
        }
        LevelConfig level = chapter.getLevel(levelIndex);
        if (level == null) {
            getAdventureView().errorLevelNotFound(levelIndex);
            return;
        }
        ChapterProgress progress = user.getChapterProgress();
        if (!progress.isChapterUnlocked(chapter.getId()) && !user.isDebugMode()) {
            getAdventureView().errorChapterLocked(chapter.getDisplayName());
            return;
        }
        if (!isLevelUnlocked(levelIndex)) {
            getAdventureView().errorLevelLocked(levelIndex);
            return;
        }
        if (level.getType() == LevelType.BOSS) {
            getAdventureView().errorBossNotImplemented();
            return;
        }
        if (!level.isPlayableNow()) {
            getAdventureView().errorSpecialNotImplemented(level.getType().name());
            return;
        }
        if (level.getType() == LevelType.CONVEYOR_BELT) {
            startConveyorBeltLevel(chapter, level);
            return;
        }
        if (level.getType() == LevelType.LOCKED_PLANTS) {
            PlantRegistry plantRegistry = App.getInstance().getPlantRegistry();
            LockedPlantsRules rules = LockedPlantsRulesFactory.create(
                    level,
                    chapter.getId(),
                    plantRegistry,
                    CollectionService.selectablePlantNames(user, plantRegistry),
                    new Random());
            LockedPlantsSelectionController selection = new LockedPlantsSelectionController(
                    user, userDatabase, chapter, level, rules);
            navigator.push(selection);
            return;
        }
        PlantSelectionController selection = new PlantSelectionController(
                user, userDatabase, chapter, level);
        navigator.push(selection);
    }

    private void startConveyorBeltLevel(ChapterConfig chapter, LevelConfig level) {
        PlantRegistry plantRegistry = App.getInstance().getPlantRegistry();
        ZombieRegistry zombieRegistry = PlantSelectionController.loadZombieRegistry();
        AdventureMode mode = new AdventureMode(
                chapter, level, plantRegistry, zombieRegistry, user.getDifficultyLevel(), new Random());
        GameSession session = mode.createSession();
        QuestTracker tracker = user.ensureQuestTracker();
        tracker.registerOn(session.getEventBus());
        tracker.beginSession(session);
        session.attachQuestTracker(tracker);
        List<String> availablePlants = CollectionService.selectablePlantNames(user, plantRegistry);
        ConveyBeltLevelController gameplay = new ConveyBeltLevelController(
                user, userDatabase, mode, session, chapter, level, Set.of(), availablePlants);
        navigator.push(gameplay);
        session.start();
    }

    private void handleShowProgress() {
        ChapterProgress progress = user.getChapterProgress();
        String text = "Unlocked through: " + progress.getUnlockedChapter().getDisplayName()
                + "\nCompleted levels in this chapter: "
                + progress.getCompletedLevels(chapter.getId());
        getAdventureView().showProgress(text);
    }

    private AdventureView getAdventureView() {
        return (AdventureView) view;
    }
}
