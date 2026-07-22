package model.minigame.mode;

import model.game.GameSession;
import model.game.mode.GameMode;
import model.minigame.MiniGameStageConfig;

public class WalnutBowlingMode extends GameMode {

    private final MiniGameStageConfig stage;

    public WalnutBowlingMode(MiniGameStageConfig stage) {
        this.stage = stage;
    }

    public MiniGameStageConfig getStage() {
        return stage;
    }

    public GameSession createSession() {
        throw new UnsupportedOperationException("Wallnut Bowling is not yet implemented");
    }
}
