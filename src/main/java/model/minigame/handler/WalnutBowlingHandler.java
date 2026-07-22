package model.minigame.handler;

import model.game.ConveyBeltHandler;
import model.game.GameSession;
import model.minigame.MiniGameHandler;
import model.minigame.MiniGameStageConfig;

import java.util.List;
import java.util.Random;

public final class WalnutBowlingHandler implements MiniGameHandler {

    private final MiniGameStageConfig stage;
    private final ConveyBeltHandler conveyorHandler;

    public WalnutBowlingHandler(MiniGameStageConfig stage, Random random) {
        if (stage == null) {
            throw new IllegalArgumentException("stage must not be null");
        }
        this.stage = stage;
        List<String> pool = stage.getConveyorPlantPool();
        this.conveyorHandler = new ConveyBeltHandler(pool, random);
    }

    @Override
    public void onLevelStart(GameSession session) {
        conveyorHandler.onLevelStart(session);
    }

    @Override
    public void onTick(GameSession session) {
        conveyorHandler.onTick(session);
    }
}
