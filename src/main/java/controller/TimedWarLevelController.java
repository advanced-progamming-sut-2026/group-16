package controller;

import model.adventure.ChapterConfig;
import model.adventure.LevelConfig;
import model.game.GameSession;
import model.game.TimedWarHandler;
import model.game.TimedWarMode;
import model.game.TimedWarRules;
import model.game.TimedWarRulesFactory;
import model.game.mode.AdventureMode;
import model.user.User;
import model.user.UserDatabase;
import view.api.TimedWarView;

import java.util.Set;

public final class TimedWarLevelController extends SpecialLevelController {

    public TimedWarLevelController(User user,
                                   UserDatabase userDatabase,
                                   AdventureController adventureController,
                                   AdventureMode adventureMode,
                                   GameSession session,
                                   ChapterConfig chapter,
                                   LevelConfig level,
                                   Set<String> boostedPlants) {
        this(user, userDatabase, adventureController, adventureMode, session, chapter, level, boostedPlants,
                TimedWarRulesFactory.create(chapter, level));
    }

    public TimedWarLevelController(User user,
                                   UserDatabase userDatabase,
                                   AdventureController adventureController,
                                   AdventureMode adventureMode,
                                   GameSession session,
                                   ChapterConfig chapter,
                                   LevelConfig level,
                                   Set<String> boostedPlants,
                                   TimedWarRules rules) {
        super(user, userDatabase, adventureController, adventureMode, session, chapter, level, boostedPlants,
                new TimedWarHandler(rules));
    }

    @Override
    public void displayMenu() {
        super.displayMenu();
        GameSession session = getSession();
        TimedWarRules rules = session.getTimedWarRules();
        if (rules == null || !session.isTimedWarActive()) {
            return;
        }
        int remainingSeconds = session.getTimedWarRemainingTicks() / GameSession.TICKS_PER_SECOND;
        getTimedWarView().showTimedWarStatus(
                rules.getMode(),
                remainingSeconds,
                rules.getDurationSeconds(),
                session.getTimedWarProgress(),
                rules.getGoalAmount());
    }

    @Override
    public void onTimedWarTimeUp() {
        getTimedWarView().showTimedWarTimeUp();
    }

    @Override
    public void onTimedWarGoalReached(TimedWarMode mode, int progress) {
        getTimedWarView().showTimedWarGoalReached(mode, progress);
    }

    private TimedWarView getTimedWarView() {
        return (TimedWarView) getView();
    }
}
