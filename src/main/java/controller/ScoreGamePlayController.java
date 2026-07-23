package controller;

import model.adventure.ChapterConfig;
import model.adventure.LevelConfig;
import model.game.GameSession;
import model.game.MatchResult;
import model.game.mode.AdventureMode;
import model.scoregame.MeowPointBreakdown;
import model.scoregame.MeowPointTracker;
import model.user.User;
import model.user.UserDatabase;

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
        super(user, userDatabase, scoreGameController, adventureMode, session, chapter, level,
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
        parser.switchController(scoreGameController);
    }
}
