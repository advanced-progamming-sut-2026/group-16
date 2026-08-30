package io.github.finalwave.controller;

import io.github.finalwave.model.App;
import io.github.finalwave.model.adventure.ChapterConfig;
import io.github.finalwave.model.adventure.LevelConfig;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.MatchResult;
import io.github.finalwave.model.game.mode.AdventureMode;
import io.github.finalwave.model.scoregame.MeowPointBreakdown;
import io.github.finalwave.model.scoregame.MeowPointTracker;
import io.github.finalwave.model.scoregame.ScoreGameSessionFactory;
import io.github.finalwave.model.user.User;
import io.github.finalwave.model.user.UserDatabase;
import io.github.finalwave.network.score.SubmitScoreOkPayload;
import io.github.finalwave.score.ScoreSubmitGateway;
import io.github.finalwave.view.api.ScoreGameView;

import java.time.Clock;
import java.util.Set;

public class ScoreGamePlayController extends GamePlayController {
    private final ScoreGameController scoreGameController;
    private final MeowPointTracker meowPointTracker;

    public ScoreGamePlayController(User user,
                                   UserDatabase userDatabase,
                                   ScoreGameController scoreGameController,
                                   AdventureMode adventureMode,
                                   GameSession session,
                                   ChapterConfig chapter,
                                   LevelConfig level,
                                   Set<String> boostedPlants,
                                   MeowPointTracker meowPointTracker) {
        super(user, userDatabase, adventureMode, session, chapter, level,
                boostedPlants, false);
        this.scoreGameController = scoreGameController;
        this.meowPointTracker = meowPointTracker;
    }

    public MeowPointTracker meowPointTracker() {
        return meowPointTracker;
    }

    @Override
    public void restartMatch() {
        meowPointTracker.unregister();
        var match = ScoreGameSessionFactory.create(
                App.getInstance().getPlantRegistry(),
                PlantSelectionController.loadZombieRegistry(),
                Clock.systemUTC());
        GameSession fresh = match.session();
        fresh.setSelectedLoadout(session().getSelectedLoadout());
        ScoreGamePlayController next = new ScoreGamePlayController(
                getUser(), getUserDatabase(), scoreGameController, match.mode(), fresh,
                match.chapter(), match.level(), boostedPlants(), match.tracker());
        navigator.replace(next);
        fresh.start();
    }

    @Override
    public void saveAndExit() {
        meowPointTracker.unregister();
        navigator.pop();
    }

    @Override
    protected void onMatchFinished(MatchResult result) {
        recordFinishedGame();
        MeowPointBreakdown breakdown = meowPointTracker.getBreakdown();
        meowPointTracker.unregister();
        ScoreSubmitGateway gateway = scoreGameController.scoreSubmitGateway();
        gateway.submit(breakdown.total(), new ScoreSubmitGateway.Callback() {
            @Override
            public void onSuccess(SubmitScoreOkPayload payload) {
                applyServerScore(payload);
                scoreGameController.onMatchCompleted(
                        breakdown,
                        payload.isHasPlayed() ? payload.getBestMeowPoint() : null,
                        payload.isNewBest());
                navigator.pop();
            }

            @Override
            public void onFailure(String reason) {
                getScoreGameView().errorSubmitFailed(reason);
                scoreGameController.onMatchCompleted(breakdown, scoreGameController.bestMeowPoint(), false);
                navigator.pop();
            }
        });
    }

    private void applyServerScore(SubmitScoreOkPayload payload) {
        User user = getUser();
        user.setHasPlayed(payload.isHasPlayed());
        user.setBestMeowPoint(payload.getBestMeowPoint());
        UserDatabase database = getUserDatabase();
        database.setWriteEventsSuppressed(true);
        try {
            database.saveBestMeowPoint(user);
        } finally {
            database.setWriteEventsSuppressed(false);
        }
    }

    private ScoreGameView getScoreGameView() {
        return (ScoreGameView) scoreGameController.getView();
    }
}
