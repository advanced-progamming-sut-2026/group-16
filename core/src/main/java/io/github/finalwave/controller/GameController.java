package io.github.finalwave.controller;

import io.github.finalwave.model.adventure.AdventureRegistry;
import io.github.finalwave.model.adventure.ChapterConfig;
import io.github.finalwave.model.adventure.ChapterId;
import io.github.finalwave.model.command.GameMenuCommands;
import io.github.finalwave.model.user.User;
import io.github.finalwave.model.user.UserDatabase;
import io.github.finalwave.view.api.GameView;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;

public class GameController extends ViewController {
    private final User user;
    private final UserDatabase userDatabase;
    public GameController(User user, UserDatabase userDatabase) {
        this.user = user;
        this.userDatabase = userDatabase;
    }

    public User getUser() {
        return user;
    }

    public List<ChapterConfig> chapters() {
        return AdventureRegistry.getInstance().getAllChapters();
    }

    public void back() {
        handleMenuExit();
    }

    public void openGreenhouse() {
        handleGreenhouse();
    }

    public void openSettings() {
        navigator.push(new SettingController(user, userDatabase));
    }

    public void openCollection() {
        navigator.push(new CollectionController(user, userDatabase));
    }

    public void openShop() {
        navigator.push(new ShopController(user, userDatabase));
    }

    public void enterChapter(ChapterId chapterId) {
        if (chapterId == null) {
            getGameView().errorUnknownChapter("");
            return;
        }
        handleEnterChapter(chapterId.getDisplayName());
    }

    public boolean hasSavedMatch() {
        return userDatabase.loadMatchSnapshot(user) != null;
    }

    public void continueSavedMatch() {
        GamePlayController gameplay = MatchResume.open(user, userDatabase, userDatabase.loadMatchSnapshot(user));
        if (gameplay == null) {
            return;
        }
        navigator.push(gameplay);
        gameplay.session().start();
    }

    public void openTravelLog() {
        handleTravelLog();
    }

    @Override
    public void displayMenu() {
        getGameView().showGameMenu();
    }

    @Override
    public void handleCommand(String input) {
        for (GameMenuCommands cmd : GameMenuCommands.values()) {
            Matcher matcher = cmd.getMatcher(input);
            if (matcher == null)
                continue;

            switch (cmd) {
                case MENU_ENTER -> handleMenuEnter(matcher.group("menuName"));
                case MENU_SHOW_CURRENT -> handleShowCurrent();
                case MENU_EXIT -> handleMenuExit();
                case ENTER_CHAPTER -> handleEnterChapter(matcher.group("chapterName"));
                case GREENHOUSE -> handleGreenhouse();
                case TRAVEL_LOG -> handleTravelLog();
                case LEADERBOARD -> handleLeaderboard();
                case COIN_WALLET -> handleCoinWallet();
                case GEM_WALLET -> handleGemWallet();
                case CHEAT_ADD -> handleCheatAdd(matcher.group("n"), matcher.group("type"));
            }
            return;
        }
        getGameView().errorInvalidCommand();
    }

    private void handleMenuEnter(String menuName) {
        String normalized = menuName.trim().toLowerCase(Locale.ROOT);
        if ("collection".equals(normalized)) {
            navigator.push(new CollectionController(user, userDatabase));
            return;
        }
        getGameView().errorNotImplemented("menu enter " + menuName);
    }

    private void handleShowCurrent() {
        getGameView().showCurrentMenu();
    }

    private void handleMenuExit() {
        navigator.pop();
    }

    private void handleEnterChapter(String chapterName) {
        var chapter = AdventureRegistry.getInstance()
                .getChapterByName(chapterName);
        if (chapter == null) {
            getGameView().errorUnknownChapter(chapterName);
            return;
        }
        if (!user.getChapterProgress().isChapterUnlocked(chapter.getId())) {
            if (!user.isDebugMode()) {
                getGameView().errorChapterLocked(chapter.getDisplayName());
                return;
            }
            user.getChapterProgress().unlockThrough(chapter.getId());
            userDatabase.saveAdventureProgress(user);
        }
        navigator.push(new AdventureController(user, userDatabase, chapter));
    }

    private void handleGreenhouse() {
        navigator.push(new GreenhouseController(user, userDatabase));
    }

    private void handleTravelLog() {
        navigator.push(new TravelLogController(user, userDatabase));
    }

    private void handleLeaderboard() {
        navigator.push(new LeaderboardController(userDatabase));
    }

    private void handleCoinWallet() {
        getGameView().showCoinWallet(user.getCoins());
    }

    private void handleGemWallet() {
        getGameView().showGemWallet(user.getDiamonds());
    }

    private void handleCheatAdd(String n, String type) {
        int amount;
        try {
            amount = Integer.parseInt(n);
        } catch (NumberFormatException e) {
            getGameView().errorInvalidCheatAmount();
            return;
        }

        if ("coin".equalsIgnoreCase(type)) {
            user.addCoins(amount);
        } else if ("diamond".equalsIgnoreCase(type)) {
            user.addDiamonds(amount);
        } else {
            getGameView().errorInvalidCheatType();
            return;
        }
        userDatabase.saveUserWallet(user);
        getGameView().showCheatAdded(type, amount);
    }

    private GameView getGameView() {
        return (GameView) view;
    }
}
