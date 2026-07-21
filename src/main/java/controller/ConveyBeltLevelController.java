package controller;

import model.adventure.ChapterConfig;
import model.adventure.LevelConfig;
import model.game.ConveyBeltHandler;
import model.game.GameSession;
import model.game.mode.AdventureMode;
import model.user.User;
import model.user.UserDatabase;

import java.util.Set;

public final class ConveyBeltLevelController extends SpecialLevelController {

    public ConveyBeltLevelController(User user,
                                     UserDatabase userDatabase,
                                     AdventureController adventureController,
                                     AdventureMode adventureMode,
                                     GameSession session,
                                     ChapterConfig chapter,
                                     LevelConfig level,
                                     Set<String> boostedPlants) {
        super(user, userDatabase, adventureController, adventureMode, session, chapter, level, boostedPlants,
                new ConveyBeltHandler());
    }
}
