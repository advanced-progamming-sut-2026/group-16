package io.github.finalwave.controller;

import io.github.finalwave.model.adventure.AdventureRegistry;
import io.github.finalwave.model.command.GameMenuCommands;
import io.github.finalwave.model.user.User;
import io.github.finalwave.model.user.UserDatabase;
import io.github.finalwave.view.api.GameView;

import java.util.Locale;
import java.util.regex.Matcher;

public class GameController extends ViewController {
    private final User user;
    private final UserDatabase userDatabase;
    private final MainMenuController mainMenuController;

    public GameController(User user, UserDatabase userDatabase, MainMenuController mainMenuController) {
        this.user = user;
        this.userDatabase = userDatabase;
        this.mainMenuController = mainMenuController;
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
            parser.switchController(new CollectionController(user, userDatabase, this));
            return;
        }
        getGameView().errorNotImplemented("menu enter " + menuName);
    }

    private void handleShowCurrent() {
        getGameView().showCurrentMenu();
    }

    private void handleMenuExit() {
        parser.switchController(mainMenuController);
    }

    private void handleEnterChapter(String chapterName) {
        var chapter = AdventureRegistry.getInstance()
                .getChapterByName(chapterName);
        if (chapter == null) {
            getGameView().errorUnknownChapter(chapterName);
            return;
        }
        if (!user.getChapterProgress().isChapterUnlocked(chapter.getId())) {
            getGameView().errorChapterLocked(chapter.getDisplayName());
            return;
        }
        parser.switchController(new AdventureController(user, userDatabase, this, chapter));
    }

    private void handleGreenhouse() {
        parser.switchController(new GreenhouseController(user, userDatabase, this));
    }

    private void handleTravelLog() {
        parser.switchController(new TravelLogController(user, userDatabase, this));
    }

    private void handleLeaderboard() {
        parser.switchController(new LeaderboardController(userDatabase, this));
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
