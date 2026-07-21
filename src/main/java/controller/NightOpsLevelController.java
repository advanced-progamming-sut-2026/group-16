package controller;

import model.adventure.ChapterConfig;
import model.adventure.LevelConfig;
import model.game.GameSession;
import model.game.NightOpsHandler;
import model.game.mode.AdventureMode;
import model.user.User;
import model.user.UserDatabase;
import view.api.NightOpsView;

import java.util.Set;

public final class NightOpsLevelController extends SpecialLevelController {

    public NightOpsLevelController(User user,
                                   UserDatabase userDatabase,
                                   AdventureController adventureController,
                                   AdventureMode adventureMode,
                                   GameSession session,
                                   ChapterConfig chapter,
                                   LevelConfig level,
                                   Set<String> boostedPlants) {
        super(user, userDatabase, adventureController, adventureMode, session, chapter, level, boostedPlants,
                new NightOpsHandler());
    }

    @Override
    public void displayMenu() {
        super.displayMenu();
        getNightOpsView().showNightOpsMode();
    }

    private NightOpsView getNightOpsView() {
        return (NightOpsView) getView();
    }
}
