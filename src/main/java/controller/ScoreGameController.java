package controller;

import model.command.ScoreGameMenuCommands;
import model.user.User;
import view.api.ScoreGameView;

import java.util.regex.Matcher;

public class ScoreGameController extends ViewController {
    private final User user;
    private final MainMenuController mainMenuController;

    public ScoreGameController(User user, MainMenuController mainMenuController) {
        this.user = user;
        this.mainMenuController = mainMenuController;
    }

    @Override
    public void displayMenu() {
        getScoreGameView().showScoreGameMenu(user.getBestMeioPoint());
    }

    @Override
    public void handleCommand(String input) {
        for (ScoreGameMenuCommands cmd : ScoreGameMenuCommands.values()) {
            Matcher matcher = cmd.getMatcher(input);
            if (matcher == null) {
                continue;
            }
            switch (cmd) {
                case MENU_SHOW_CURRENT -> getScoreGameView().showCurrentMenu();
                case MENU_EXIT -> parser.switchController(mainMenuController);
            }
            return;
        }
        getScoreGameView().errorInvalidCommand();
    }

    private ScoreGameView getScoreGameView() {
        return (ScoreGameView) view;
    }
}
