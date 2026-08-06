package io.github.finalwave.controller;

import io.github.finalwave.model.adventure.ChapterConfig;
import io.github.finalwave.model.adventure.LevelConfig;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.MatchResult;
import io.github.finalwave.model.game.mode.AdventureMode;
import io.github.finalwave.model.scoregame.MeowPointBreakdown;
import io.github.finalwave.model.scoregame.MeowPointTracker;
import io.github.finalwave.model.user.User;
import io.github.finalwave.model.user.UserDatabase;

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
