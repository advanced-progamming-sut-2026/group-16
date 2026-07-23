package controller;

import model.command.ScoreGameMenuCommands;
import model.scoregame.MeowPointBreakdown;
import model.user.User;
import model.user.UserDatabase;
import view.api.ScoreGameView;

import java.util.regex.Matcher;

public class ScoreGameController extends ViewController {
    private final User user;
    private final UserDatabase userDatabase;
    private final MainMenuController mainMenuController;
    private MeowPointBreakdown pendingBreakdown;
    private boolean pendingNewBest;

    public ScoreGameController(User user, UserDatabase userDatabase,
                               MainMenuController mainMenuController) {
        this.user = user;
        this.userDatabase = userDatabase;
        this.mainMenuController = mainMenuController;
    }

    public ScoreGameController(User user, MainMenuController mainMenuController) {
        this(user, UserDatabase.getInstance(), mainMenuController);
    }

    @Override
    public void displayMenu() {
        getScoreGameView().showScoreGameMenu(user.getBestMeowPoint());
        if (pendingBreakdown != null) {
            getScoreGameView().showMatchResult(
                    pendingBreakdown, user.getBestMeowPoint(), pendingNewBest);
            pendingBreakdown = null;
            pendingNewBest = false;
        }
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
                case START -> parser.switchController(
                        new ScoreGamePlantSelectionController(user, userDatabase, this));
            }
            return;
        }
        getScoreGameView().errorInvalidCommand();
    }

    public void onMatchCompleted(MeowPointBreakdown breakdown, boolean newBest) {
        this.pendingBreakdown = breakdown;
        this.pendingNewBest = newBest;
    }

    private ScoreGameView getScoreGameView() {
        return (ScoreGameView) view;
    }
}
