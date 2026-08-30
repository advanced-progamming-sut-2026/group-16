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
        fresh.setSelectedLoadoutOrder(session().getSelectedLoadoutOrder());
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
        boolean newBest = getUser().updateBestMeowPoint(breakdown.total());
        if (newBest) {
            getUserDatabase().saveBestMeowPoint(getUser());
        }
        meowPointTracker.unregister();
        scoreGameController.onMatchCompleted(breakdown, newBest);
        navigator.pop();
    }
}
