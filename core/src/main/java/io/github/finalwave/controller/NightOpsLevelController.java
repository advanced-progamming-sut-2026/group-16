package io.github.finalwave.controller;

import io.github.finalwave.model.adventure.ChapterConfig;
import io.github.finalwave.model.adventure.LevelConfig;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.NightOpsHandler;
import io.github.finalwave.model.game.mode.AdventureMode;
import io.github.finalwave.model.user.User;
import io.github.finalwave.model.user.UserDatabase;
import io.github.finalwave.view.api.NightOpsView;

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
