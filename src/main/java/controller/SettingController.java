package controller;

import model.command.SettingMenuCommands;
import model.user.User;
import model.user.UserDatabase;
import view.api.SettingView;

import java.util.regex.Matcher;

public class SettingController extends ViewController {
    private final User user;
    private final UserDatabase userDatabase;
    private final MainMenuController mainMenuController;

    public SettingController(User user, UserDatabase userDatabase, MainMenuController mainMenuController) {
        this.user = user;
        this.userDatabase = userDatabase;
        this.mainMenuController = mainMenuController;
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
        parser.switchController(mainMenuController);
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
