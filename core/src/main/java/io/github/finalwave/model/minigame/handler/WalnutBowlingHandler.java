package io.github.finalwave.model.minigame.handler;

import io.github.finalwave.model.game.ConveyBeltHandler;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.minigame.MiniGameHandler;
import io.github.finalwave.model.minigame.MiniGameStageConfig;

import java.util.List;
import java.util.Random;

public final class WalnutBowlingHandler implements MiniGameHandler {

    private final ConveyBeltHandler conveyorHandler;

    public WalnutBowlingHandler(MiniGameStageConfig stage, Random random) {
        if (stage == null) {
            throw new IllegalArgumentException("stage must not be null");
        }
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
