package io.github.finalwave.controller;

import io.github.finalwave.model.adventure.ChapterConfig;
import io.github.finalwave.model.adventure.LevelConfig;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.TimedWarHandler;
import io.github.finalwave.model.game.TimedWarMode;
import io.github.finalwave.model.game.TimedWarRules;
import io.github.finalwave.model.game.TimedWarRulesFactory;
import io.github.finalwave.model.game.mode.AdventureMode;
import io.github.finalwave.model.user.User;
import io.github.finalwave.model.user.UserDatabase;
import io.github.finalwave.view.api.TimedWarView;

import java.util.Set;

public final class TimedWarLevelController extends SpecialLevelController {

    public TimedWarLevelController(User user,
                                   UserDatabase userDatabase,
                                   AdventureMode adventureMode,
                                   GameSession session,
                                   ChapterConfig chapter,
                                   LevelConfig level,
                                   Set<String> boostedPlants) {
        this(user, userDatabase, adventureMode, session, chapter, level, boostedPlants,
                TimedWarRulesFactory.create(chapter, level));
    }

    public TimedWarLevelController(User user,
                                   UserDatabase userDatabase,
                                   AdventureMode adventureMode,
                                   GameSession session,
                                   ChapterConfig chapter,
                                   LevelConfig level,
                                   Set<String> boostedPlants,
                                   TimedWarRules rules) {
        super(user, userDatabase, adventureMode, session, chapter, level, boostedPlants,
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
