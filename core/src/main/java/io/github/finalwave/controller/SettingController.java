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
                case MENU_EXIT -> handleMenuExit();
                case CHANGE_DIFFICULTY -> handleChangeDifficulty(matcher.group("difficultyLevel"));
            }
            return;
        }
        getSettingView().errorInvalidCommand();
    }

    private void handleShowCurrent() {
        getSettingView().showCurrentMenu(user.getDifficultyLevel());
    }

    private void handleMenuExit() {
        navigator.pop();
    }

    private void handleChangeDifficulty(String difficultyLevel) {
        int level;
        try {
            level = Integer.parseInt(difficultyLevel.trim());
        } catch (NumberFormatException e) {
            getSettingView().errorInvalidDifficultyFormat();
            return;
        }
        if (level < 1 || level > 5) {
            getSettingView().errorDifficultyOutOfRange();
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
