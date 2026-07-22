package model.minigame.mode;

import model.game.GameSession;
import model.game.mode.GameMode;
import model.minigame.MiniGameStageConfig;

public class IZombieMode extends GameMode {

    private final MiniGameStageConfig stage;

    public IZombieMode(MiniGameStageConfig stage) {
        this.stage = stage;
    }

    public MiniGameStageConfig getStage() {
        return stage;
    }

    public GameSession createSession() {
        throw new UnsupportedOperationException("I, Zombie is not yet implemented");
    }
}
