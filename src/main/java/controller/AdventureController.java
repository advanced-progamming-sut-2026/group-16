package controller;

import model.App;
import model.adventure.ChapterConfig;
import model.adventure.LevelConfig;
import model.adventure.LevelType;
import model.command.AdventureMenuCommands;
import model.definition.PlantRegistry;
import model.definition.ZombieRegistry;
import model.game.GameSession;
import model.game.mode.AdventureMode;
import model.quest.QuestTracker;
import model.user.ChapterProgress;
import model.user.User;
import model.user.UserDatabase;
import view.api.AdventureView;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;

public class AdventureController extends ViewController {

    private final User user;
    private final UserDatabase userDatabase;
    private final GameController gameController;
    private final ChapterConfig chapter;

    public AdventureController(User user,
                               UserDatabase userDatabase,
                               GameController gameController,
                               ChapterConfig chapter) {
        this.user = user;
        this.userDatabase = userDatabase;
        this.gameController = gameController;
        this.chapter = chapter;
    }

    public ChapterConfig getChapter() {
        return chapter;
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
                case MENU_EXIT -> parser.switchController(gameController);
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
        if (!progress.isChapterUnlocked(chapter.getId())) {
            getAdventureView().errorChapterLocked(chapter.getDisplayName());
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
        PlantSelectionController selection = new PlantSelectionController(
                user, userDatabase, this, chapter, level);
        parser.switchController(selection);
    }

    private void startConveyorBeltLevel(ChapterConfig chapter, LevelConfig level) {
        PlantRegistry plantRegistry = App.getInstance().getPlantRegistry();
        ZombieRegistry zombieRegistry = PlantSelectionController.loadZombieRegistry();
        AdventureMode mode = new AdventureMode(
                chapter, level, plantRegistry, zombieRegistry, user.getDifficultyLevel(), new Random());
        GameSession session = mode.createSession();
        QuestTracker tracker = user.ensureQuestTracker();
        tracker.registerOn(session.getEventBus());
        tracker.beginSession();
        session.attachQuestTracker(tracker);
        List<String> availablePlants = user.getPlantProgress().getUnlockedPlantNames();
        ConveyBeltLevelController gameplay = new ConveyBeltLevelController(
                user, userDatabase, this, mode, session, chapter, level, Set.of(), availablePlants);
        parser.switchController(gameplay);
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
