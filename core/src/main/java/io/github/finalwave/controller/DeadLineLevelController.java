package io.github.finalwave.controller;

import io.github.finalwave.model.adventure.ChapterConfig;
import io.github.finalwave.model.adventure.LevelConfig;
import io.github.finalwave.model.game.DeadLineHandler;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.mode.AdventureMode;
import io.github.finalwave.model.user.User;
import io.github.finalwave.model.user.UserDatabase;
import io.github.finalwave.view.api.DeadLineView;

import java.util.Set;

public final class DeadLineLevelController extends SpecialLevelController {

    public DeadLineLevelController(User user,
                                   UserDatabase userDatabase,
                                   AdventureMode adventureMode,
                                   GameSession session,
                                   ChapterConfig chapter,
                                   LevelConfig level,
                                   Set<String> boostedPlants) {
        this(user, userDatabase, adventureMode, session, chapter, level, boostedPlants,
                new DeadLineHandler());
    }

    DeadLineLevelController(User user,
                            UserDatabase userDatabase,
                            AdventureMode adventureMode,
                            GameSession session,
                            ChapterConfig chapter,
                            LevelConfig level,
                            Set<String> boostedPlants,
                            DeadLineHandler handler) {
        super(user, userDatabase, adventureMode, session, chapter, level, boostedPlants,
                handler);
    }

    @Override
    public void displayMenu() {
        super.displayMenu();
        if (getSession().isDeadLineActive()) {
            getDeadLineView().showDeadLineRule(getSession().getDeadLineColumn());
        }
    }

    @Override
    public void onDeadLineBreached(int column, String zombieType) {
        getDeadLineView().showDeadLineBreached(column, zombieType);
    }

    private DeadLineView getDeadLineView() {
        return (DeadLineView) getView();
    }
}
