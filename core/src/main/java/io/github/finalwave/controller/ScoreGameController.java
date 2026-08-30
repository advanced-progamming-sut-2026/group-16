package io.github.finalwave.controller;

import io.github.finalwave.model.command.ScoreGameMenuCommands;
import io.github.finalwave.model.scoregame.MeowPointBreakdown;
import io.github.finalwave.model.user.User;
import io.github.finalwave.model.user.UserDatabase;
import io.github.finalwave.score.ScoreSubmitGateway;
import io.github.finalwave.view.api.ScoreGameView;

import java.util.regex.Matcher;

public class ScoreGameController extends ViewController {
    private final User user;
    private final UserDatabase userDatabase;
    private final ScoreSubmitGateway scoreSubmitGateway;
    private MeowPointBreakdown pendingBreakdown;
    private Integer pendingBestMeowPoint;
    private boolean pendingNewBest;

    public ScoreGameController(User user, UserDatabase userDatabase, ScoreSubmitGateway scoreSubmitGateway) {
        this.user = user;
        this.userDatabase = userDatabase;
        this.scoreSubmitGateway = scoreSubmitGateway;
    }

    public ScoreGameController(User user, ScoreSubmitGateway scoreSubmitGateway) {
        this(user, UserDatabase.getInstance(), scoreSubmitGateway);
    }

    public User getUser() {
        return user;
    }

    public ScoreSubmitGateway scoreSubmitGateway() {
        return scoreSubmitGateway;
    }

    public Integer bestMeowPoint() {
        return user.hasPlayed() ? user.getBestMeowPoint() : null;
    }

    public void startMatch() {
        navigator.push(new ScoreGamePlantSelectionController(user, userDatabase, this));
    }

    public void back() {
        navigator.pop();
    }

    @Override
    public void displayMenu() {
        getScoreGameView().showScoreGameMenu(bestMeowPoint());
        if (pendingBreakdown != null) {
            getScoreGameView().showMatchResult(
                    pendingBreakdown, pendingBestMeowPoint, pendingNewBest);
            pendingBreakdown = null;
            pendingBestMeowPoint = null;
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
                case MENU_EXIT -> navigator.pop();
                case START -> navigator.push(
                        new ScoreGamePlantSelectionController(user, userDatabase, this));
            }
            return;
        }
        getScoreGameView().errorInvalidCommand();
    }

    public void onMatchCompleted(MeowPointBreakdown breakdown, Integer bestMeowPoint, boolean newBest) {
        this.pendingBreakdown = breakdown;
        this.pendingBestMeowPoint = bestMeowPoint;
        this.pendingNewBest = newBest;
    }

    private ScoreGameView getScoreGameView() {
        return (ScoreGameView) view;
    }
}
