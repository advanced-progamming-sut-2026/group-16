package model.minigame.mode;

import model.game.GameSession;
import model.game.mode.GameMode;
import model.minigame.MiniGameStageConfig;

public class ZombotanyMode extends GameMode {

    private final MiniGameStageConfig stage;

    public ZombotanyMode(MiniGameStageConfig stage) {
        this.stage = stage;
    }

    public MiniGameStageConfig getStage() {
        return stage;
    }

    public GameSession createSession() {
        throw new UnsupportedOperationException("Zombotany is not yet implemented");
    }
}
