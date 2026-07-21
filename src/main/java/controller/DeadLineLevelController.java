package controller;

import model.adventure.ChapterConfig;
import model.adventure.LevelConfig;
import model.game.DeadLineHandler;
import model.game.GameSession;
import model.game.mode.AdventureMode;
import model.user.User;
import model.user.UserDatabase;
import view.api.DeadLineView;

import java.util.Set;

public final class DeadLineLevelController extends SpecialLevelController {

    public DeadLineLevelController(User user,
                                   UserDatabase userDatabase,
                                   AdventureController adventureController,
                                   AdventureMode adventureMode,
                                   GameSession session,
                                   ChapterConfig chapter,
                                   LevelConfig level,
                                   Set<String> boostedPlants) {
        this(user, userDatabase, adventureController, adventureMode, session, chapter, level, boostedPlants,
                new DeadLineHandler());
    }

    DeadLineLevelController(User user,
                            UserDatabase userDatabase,
                            AdventureController adventureController,
                            AdventureMode adventureMode,
                            GameSession session,
                            ChapterConfig chapter,
                            LevelConfig level,
                            Set<String> boostedPlants,
                            DeadLineHandler handler) {
        super(user, userDatabase, adventureController, adventureMode, session, chapter, level, boostedPlants,
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
