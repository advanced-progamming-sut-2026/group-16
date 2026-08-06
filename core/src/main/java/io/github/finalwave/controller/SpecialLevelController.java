package io.github.finalwave.controller;

import io.github.finalwave.model.adventure.ChapterConfig;
import io.github.finalwave.model.adventure.LevelConfig;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.SpecialLevelHandler;
import io.github.finalwave.model.game.mode.AdventureMode;
import io.github.finalwave.model.user.User;
import io.github.finalwave.model.user.UserDatabase;

import java.util.Set;

public abstract class SpecialLevelController extends GamePlayController {

    private final SpecialLevelHandler handler;

    protected SpecialLevelController(User user,
                                     UserDatabase userDatabase,
                                     AdventureMode adventureMode,
                                     GameSession session,
                                     ChapterConfig chapter,
                                     LevelConfig level,
                                     Set<String> boostedPlants,
                                     SpecialLevelHandler handler) {
        super(user, userDatabase, adventureMode, session, chapter, level, boostedPlants);
        this.handler = handler;
        session.setActiveSpecialLevelHandler(handler);
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
