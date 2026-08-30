package io.github.finalwave.model.minigame.izombie;

import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.MatchResult;
import io.github.finalwave.model.game.entity.zombie.Zombie;
import io.github.finalwave.model.minigame.MiniGameHandler;
import io.github.finalwave.model.minigame.MiniGameStageConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class IZombieHandler implements MiniGameHandler {

    public static final String SUN_PRODUCER_ALIAS = "ZombieArmor2";

    private final MiniGameStageConfig stage;
    private final Random random;
    private final SunProducerSystem sunProducerSystem = new SunProducerSystem();

    public IZombieHandler(MiniGameStageConfig stage, Random random) {
        if (stage == null) {
            throw new IllegalArgumentException("stage must not be null");
        }
        this.stage = stage;
        this.random = random == null ? new Random() : random;
    }

    public SunProducerSystem getSunProducerSystem() {
        return sunProducerSystem;
    }

    @Override
    public void onLevelStart(GameSession session) {
        spawnSunProducers(session);
        placeDefensePlants(session);
    }

    @Override
    public void onTick(GameSession session) {
        if (session.getMatchResult() != MatchResult.IN_PROGRESS) {
            return;
        }
        sunProducerSystem.tick(session);
        if (session.areAllIZombieBrainsEaten()) {
            session.winMatch();
            return;
        }
        if (!hasLivingZombies(session)
                && session.getIZombieSunBalance() < session.getIZombieCheapestRosterCost()) {
            session.loseMatch();
        }
    }

    private void spawnSunProducers(GameSession session) {
        int rightmostCol = session.getBoard().getCols() - 1;
        for (int row = 0; row < session.getBoard().getRows(); row++) {
            Zombie producer = session.spawnZombieOfType(SUN_PRODUCER_ALIAS, row, rightmostCol);
            producer.setStationary(true);
            producer.lockLane();
            sunProducerSystem.register(producer, row);
        }
    }

    private void placeDefensePlants(GameSession session) {
        List<String> plantPool = stage.getPlantSeedPool();
        if (plantPool.isEmpty()) {
            return;
        }
        List<int[]> cells = new ArrayList<>();
        int maxCol = Math.min(stage.getRedLineColumn(), session.getBoard().getCols()) - 1;
        if (maxCol < 0) {
            return;
        }
        for (int row = 0; row < stage.getRows(); row++) {
            for (int col = 0; col <= maxCol; col++) {
                cells.add(new int[]{col, row});
            }
        }
        Collections.shuffle(cells, random);
        int count = Math.min(stage.getPrePlantedPlantCount(), cells.size());
        for (int i = 0; i < count; i++) {
            int[] cell = cells.get(i);
            String plantName = plantPool.get(random.nextInt(plantPool.size()));
            session.placeDefensePlant(plantName, cell[0], cell[1]);
        }
    }

    private static boolean hasLivingZombies(GameSession session) {
        for (Zombie zombie : session.getZombies()) {
            if (zombie.isAlive()) {
                return true;
            }
        }
        return false;
    }
}
