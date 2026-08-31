package io.github.finalwave.model.minigame.izombie;

import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.MatchResult;
import io.github.finalwave.model.game.entity.zombie.Zombie;
import io.github.finalwave.model.minigame.MiniGameHandler;
import io.github.finalwave.model.minigame.MiniGameStageConfig;

public final class NetworkedIZombieHandler implements MiniGameHandler {

    public static final int NETWORKED_IZOMBIE_PLANT_WIN_SECONDS = IZombieDuelCatalog.ROUND_SECONDS;

    private final MiniGameStageConfig stage;
    private final SunProducerSystem sunProducerSystem = new SunProducerSystem();
    private boolean playing;
    private boolean producersSpawned;
    private double elapsedSeconds;

    public NetworkedIZombieHandler(MiniGameStageConfig stage) {
        if (stage == null) {
            throw new IllegalArgumentException("stage must not be null");
        }
        this.stage = stage;
    }

    public MiniGameStageConfig getStage() {
        return stage;
    }

    public boolean isPlaying() {
        return playing;
    }

    public double getElapsedSeconds() {
        return elapsedSeconds;
    }

    public int secondsLeft() {
        return Math.max(0, NETWORKED_IZOMBIE_PLANT_WIN_SECONDS - (int) elapsedSeconds);
    }

    public SunProducerSystem getSunProducerSystem() {
        return sunProducerSystem;
    }

    public void beginPlay(GameSession session) {
        playing = true;
        elapsedSeconds = 0d;
        spawnSunProducers(session);
    }

    public void beginPlay() {
        playing = true;
        elapsedSeconds = 0d;
    }

    @Override
    public void onLevelStart(GameSession session) {
    }

    @Override
    public void onTick(GameSession session) {
        if (!playing || session.getMatchResult() != MatchResult.IN_PROGRESS) {
            return;
        }
        if (!producersSpawned) {
            spawnSunProducers(session);
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
        if (session == null || producersSpawned) {
            return;
        }
        producersSpawned = true;
        int rightmostCol = session.getBoard().getCols() - 1;
        for (int row = 0; row < session.getBoard().getRows(); row++) {
            Zombie producer = session.spawnZombieOfType(IZombieHandler.SUN_PRODUCER_ALIAS, row, rightmostCol);
            if (producer == null) {
                continue;
            }
            producer.setStationary(true);
            producer.lockLane();
            sunProducerSystem.register(producer, row);
        }
    }
}
