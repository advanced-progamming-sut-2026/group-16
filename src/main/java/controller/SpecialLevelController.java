package controller;

import model.adventure.ChapterConfig;
import model.adventure.LevelConfig;
import model.game.GameSession;
import model.game.SpecialLevelHandler;
import model.game.mode.AdventureMode;
import model.user.User;
import model.user.UserDatabase;

import java.util.Set;

public abstract class SpecialLevelController extends GamePlayController {

    private final SpecialLevelHandler handler;

    protected SpecialLevelController(User user,
                                     UserDatabase userDatabase,
                                     AdventureController adventureController,
                                     AdventureMode adventureMode,
                                     GameSession session,
                                     ChapterConfig chapter,
                                     LevelConfig level,
                                     Set<String> boostedPlants,
                                     SpecialLevelHandler handler) {
        super(user, userDatabase, adventureController, adventureMode, session, chapter, level, boostedPlants);
        this.handler = handler;
        this.handler.onLevelStart(getSession());
    }

    protected SpecialLevelHandler getHandler() {
        return handler;
    }

    @Override
    public void onWaveStarted(int waveNumber) {
        super.onWaveStarted(waveNumber);
        handler.onWaveStarted(getSession(), waveNumber);
    }

    @Override
    public void onWin() {
        super.onWin();
        handler.onLevelWon(getSession());
    }

    @Override
    public void onLose() {
        super.onLose();
        handler.onLevelLost(getSession());
    }
}
