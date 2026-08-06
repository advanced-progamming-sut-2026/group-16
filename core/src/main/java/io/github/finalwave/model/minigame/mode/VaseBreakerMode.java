package io.github.finalwave.model.minigame.mode;

import io.github.finalwave.model.definition.PlantRegistry;
import io.github.finalwave.model.definition.ZombieRegistry;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.board.GameBoard;
import io.github.finalwave.model.game.entity.Vase;
import io.github.finalwave.model.game.mode.GameMode;
import io.github.finalwave.model.minigame.MiniGameStageConfig;
import io.github.finalwave.model.minigame.handler.VaseBreakerHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class VaseBreakerMode extends GameMode {

    private final MiniGameStageConfig stage;
    private final PlantRegistry plantRegistry;
    private final ZombieRegistry zombieRegistry;
    private final Random random;

    public VaseBreakerMode(MiniGameStageConfig stage,
                           PlantRegistry plantRegistry,
                           ZombieRegistry zombieRegistry,
                           Random random) {
        if (stage == null || plantRegistry == null || zombieRegistry == null) {
            throw new IllegalArgumentException("vasebreaker mode dependencies must not be null");
        }
        this.stage = stage;
        this.plantRegistry = plantRegistry;
        this.zombieRegistry = zombieRegistry;
        this.random = random == null ? new Random() : random;
    }

    public MiniGameStageConfig getStage() {
        return stage;
    }

    public GameSession createSession() {
        GameBoard board = new GameBoard(stage.getRows(), stage.getCols());
        GameSession session = new GameSession(
                plantRegistry, board, stage.getStartingSun(), zombieRegistry, 1, random);
        session.setChapterId("minigame");
        session.setLevelId("vase-breaker-S" + stage.getStageIndex());
        session.getSkySunSystem().setEnabled(false);
        session.setWavesAutoStart(false);
        session.setSeedPacketExpiryTicks(stage.getSeedPacketExpiryTicks());
        session.setActiveMiniGameHandler(new VaseBreakerHandler());
        placeVases(session);
        return session;
    }

    private void placeVases(GameSession session) {
        List<int[]> cells = new ArrayList<>();
        for (int row = 0; row < stage.getRows(); row++) {
            for (int col = 1; col < stage.getCols(); col++) {
                cells.add(new int[]{col, row});
            }
        }
        Collections.shuffle(cells, random);
        int potCount = Math.min(stage.getPotCount(), cells.size());
        int gargCount = Math.min(stage.getGargantuarPotCount(), potCount);
        int plantSeedCount = Math.min(stage.getPlantSeedPotCount(), potCount - gargCount);
        int remaining = potCount - gargCount - plantSeedCount;

        int index = 0;
        for (int i = 0; i < gargCount; i++) {
            int[] cell = cells.get(index++);
            session.addVase(new Vase(
                    "vase-" + cell[0] + "-" + cell[1],
                    cell[0], cell[1],
                    Vase.Content.GARGANTUAR,
                    "ZombieGargantuar"));
        }
        for (int i = 0; i < plantSeedCount; i++) {
            int[] cell = cells.get(index++);
            session.addVase(new Vase(
                    "vase-" + cell[0] + "-" + cell[1],
                    cell[0], cell[1],
                    Vase.Content.PLANT_SEED,
                    pickPlant()));
        }
        for (int i = 0; i < remaining; i++) {
            int[] cell = cells.get(index++);
            session.addVase(createNormalVase(cell[0], cell[1]));
        }
    }

    private Vase createNormalVase(int col, int row) {
        int roll = random.nextInt(100);
        if (roll < 25) {
            return new Vase("vase-" + col + "-" + row, col, row, Vase.Content.EMPTY, null);
        }
        if (roll < 55) {
            return new Vase("vase-" + col + "-" + row, col, row, Vase.Content.PLANT_SEED, pickPlant());
        }
        return new Vase("vase-" + col + "-" + row, col, row, Vase.Content.ZOMBIE, pickZombie());
    }

    private String pickPlant() {
        List<String> pool = stage.getPlantSeedPool();
        if (pool.isEmpty()) {
            return "Peashooter";
        }
        return pool.get(random.nextInt(pool.size()));
    }

    private String pickZombie() {
        List<String> pool = stage.getZombiePool();
        if (pool.isEmpty()) {
            return "ZombieDefault";
        }
        return pool.get(random.nextInt(pool.size()));
    }
}
