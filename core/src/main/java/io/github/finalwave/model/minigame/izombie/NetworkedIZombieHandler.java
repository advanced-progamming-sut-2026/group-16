package io.github.finalwave.model.minigame.izombie;

import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.MatchResult;
import io.github.finalwave.model.game.entity.zombie.Zombie;
import io.github.finalwave.model.minigame.MiniGameHandler;
import io.github.finalwave.model.minigame.MiniGameStageConfig;

public final class NetworkedIZombieHandler implements MiniGameHandler {

    public static final int NETWORKED_IZOMBIE_PLANT_WIN_SECONDS = 120;

    private final MiniGameStageConfig stage;
    private final SunProducerSystem sunProducerSystem = new SunProducerSystem();
    private double elapsedSeconds;

    public NetworkedIZombieHandler(MiniGameStageConfig stage) {
        if (stage == null) {
            throw new IllegalArgumentException("stage must not be null");
        }
        this.stage = stage;
    }

    public double getElapsedSeconds() {
        return elapsedSeconds;
    }

    @Override
    public void onLevelStart(GameSession session) {
        spawnSunProducers(session);
    }

    @Override
    public void onTick(GameSession session) {
        if (session.getMatchResult() != MatchResult.IN_PROGRESS) {
            return;
        }
        sunProducerSystem.tick(session);
        elapsedSeconds += 1.0 / GameSession.TICKS_PER_SECOND;
        if (session.areAllIZombieBrainsEaten()) {
            session.loseMatch();
            return;
        }
        if (elapsedSeconds >= NETWORKED_IZOMBIE_PLANT_WIN_SECONDS) {
            session.winMatch();
        }
    }

    private void spawnSunProducers(GameSession session) {
        int rightmostCol = session.getBoard().getCols() - 1;
        for (int row = 0; row < session.getBoard().getRows(); row++) {
            Zombie producer = session.spawnZombieOfType(IZombieHandler.SUN_PRODUCER_ALIAS, row, rightmostCol);
            producer.setStationary(true);
            producer.lockLane();
            sunProducerSystem.register(producer, row);
        }
    }
}
