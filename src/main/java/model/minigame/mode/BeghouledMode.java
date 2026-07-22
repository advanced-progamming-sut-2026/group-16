package model.minigame.mode;

import model.game.GameSession;
import model.game.mode.GameMode;
import model.minigame.MiniGameStageConfig;

public class BeghouledMode extends GameMode {

    private final MiniGameStageConfig stage;

    public BeghouledMode(MiniGameStageConfig stage) {
        this.stage = stage;
    }

    public MiniGameStageConfig getStage() {
        return stage;
    }

    public GameSession createSession() {
        throw new UnsupportedOperationException("Beghouled is not yet implemented");
    }
}
