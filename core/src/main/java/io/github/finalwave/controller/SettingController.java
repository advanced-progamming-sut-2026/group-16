package io.github.finalwave.controller;

import io.github.finalwave.model.command.SettingMenuCommands;
import io.github.finalwave.model.user.User;
import io.github.finalwave.model.user.UserDatabase;
import io.github.finalwave.view.api.SettingView;

import java.util.regex.Matcher;

public class SettingController extends ViewController {
    private final User user;
    private final UserDatabase userDatabase;

    public SettingController(User user, UserDatabase userDatabase) {
        this.user = user;
        this.userDatabase = userDatabase;
    }

    public User getUser() {
        return user;
    }

    @Override
    public void displayMenu() {
        getSettingView().showSettingsMenu(user.getDifficultyLevel());
    }

    @Override
    public void handleCommand(String input) {
        for (SettingMenuCommands cmd : SettingMenuCommands.values()) {
            Matcher matcher = cmd.getMatcher(input);
            if (matcher == null) {
                continue;
            }

            switch (cmd) {
                case MENU_SHOW_CURRENT -> handleShowCurrent();
                case MENU_EXIT -> back();
                case CHANGE_DIFFICULTY -> handleChangeDifficulty(matcher.group("difficultyLevel"));
            }
            return;
        }
        getSettingView().errorInvalidCommand();
    }

    public void back() {
        navigator.pop();
    }

    public void changeDifficulty(int level) {
        applyDifficulty(level);
    }

    public void setGameSpeed(int speed) {
        int clamped = Math.max(1, Math.min(3, speed));
        if (user.getGameSpeed() == clamped) {
            return;
        }
        user.setGameSpeed(clamped);
        userDatabase.saveUserSettings(user);
        getSettingView().showSettingsMenu(user.getDifficultyLevel());
    }

    public void setShowLawnGrid(boolean showLawnGrid) {
        if (user.isShowLawnGrid() == showLawnGrid) {
            return;
        }
        user.setShowLawnGrid(showLawnGrid);
        userDatabase.saveUserSettings(user);
        getSettingView().showSettingsMenu(user.getDifficultyLevel());
    }

    public void setDebugMode(boolean debugMode) {
        if (user.isDebugMode() == debugMode) {
            return;
        }
        user.setDebugMode(debugMode);
        userDatabase.saveUserSettings(user);
        getSettingView().showSettingsMenu(user.getDifficultyLevel());
    }

    private void handleShowCurrent() {
        getSettingView().showCurrentMenu(user.getDifficultyLevel());
    }

    private void handleChangeDifficulty(String difficultyLevel) {
        int level;
        try {
            level = Integer.parseInt(difficultyLevel.trim());
        } catch (NumberFormatException e) {
            getSettingView().errorInvalidDifficultyFormat();
            return;
        }
        applyDifficulty(level);
    }

    private void applyDifficulty(int level) {
        if (level < 1 || level > 5) {
            getSettingView().errorDifficultyOutOfRange();
            return;
        }
        if (user.getDifficultyLevel() == level) {
            return;
        }
        user.setDifficultyLevel(level);
        userDatabase.saveAdventureProgress(user);
        getSettingView().showChangedDifficulty(user.getDifficultyLevel());
    }

    private SettingView getSettingView() {
        return (SettingView) view;
    }
}
