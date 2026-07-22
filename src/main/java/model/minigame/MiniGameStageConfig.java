package model.minigame;

import java.util.List;

public final class MiniGameStageConfig {

    private final MiniGameId miniGameId;
    private final int stageIndex;
    private final int rows;
    private final int cols;
    private final int startingSun;
    private final int potCount;
    private final int plantSeedPotCount;
    private final int gargantuarPotCount;
    private final int seedPacketExpiryTicks;
    private final List<String> plantSeedPool;
    private final List<String> zombiePool;
    private final boolean implemented;

    public MiniGameStageConfig(MiniGameId miniGameId,
                               int stageIndex,
                               int rows,
                               int cols,
                               int startingSun,
                               int potCount,
                               int plantSeedPotCount,
                               int gargantuarPotCount,
                               int seedPacketExpiryTicks,
                               List<String> plantSeedPool,
                               List<String> zombiePool,
                               boolean implemented) {
        if (miniGameId == null) {
            throw new IllegalArgumentException("miniGameId must not be null");
        }
        if (stageIndex < 1) {
            throw new IllegalArgumentException("stageIndex must be >= 1");
        }
        this.miniGameId = miniGameId;
        this.stageIndex = stageIndex;
        this.rows = Math.max(1, rows);
        this.cols = Math.max(1, cols);
        this.startingSun = Math.max(0, startingSun);
        this.potCount = Math.max(0, potCount);
        this.plantSeedPotCount = Math.max(0, plantSeedPotCount);
        this.gargantuarPotCount = Math.max(0, gargantuarPotCount);
        this.seedPacketExpiryTicks = Math.max(1, seedPacketExpiryTicks);
        this.plantSeedPool = plantSeedPool == null ? List.of() : List.copyOf(plantSeedPool);
        this.zombiePool = zombiePool == null ? List.of() : List.copyOf(zombiePool);
        this.implemented = implemented;
    }

    public MiniGameId getMiniGameId() {
        return miniGameId;
    }

    public int getStageIndex() {
        return stageIndex;
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public int getStartingSun() {
        return startingSun;
    }

    public int getPotCount() {
        return potCount;
    }

    public int getPlantSeedPotCount() {
        return plantSeedPotCount;
    }

    public int getGargantuarPotCount() {
        return gargantuarPotCount;
    }

    public int getSeedPacketExpiryTicks() {
        return seedPacketExpiryTicks;
    }

    public List<String> getPlantSeedPool() {
        return plantSeedPool;
    }

    public List<String> getZombiePool() {
        return zombiePool;
    }

    public boolean isImplemented() {
        return implemented;
    }

    public static MiniGameStageConfig vaseBreaker(int stageIndex) {
        List<String> plants = List.of(
                "Peashooter", "Sunflower", "Wall-nut", "Potato Mine", "Cabbage-pult");
        List<String> zombies = List.of("ZombieDefault", "ZombieArmor1");
        return switch (stageIndex) {
            case 1 -> new MiniGameStageConfig(
                    MiniGameId.VASE_BREAKER, 1, 5, 9, 0,
                    12, 5, 0, 150, plants, zombies, true);
            case 2 -> new MiniGameStageConfig(
                    MiniGameId.VASE_BREAKER, 2, 5, 9, 0,
                    18, 7, 1, 100, plants, List.of("ZombieDefault", "ZombieArmor1", "ZombieArmor2"),
                    true);
            case 3 -> new MiniGameStageConfig(
                    MiniGameId.VASE_BREAKER, 3, 5, 9, 0,
                    24, 8, 2, 70, plants,
                    List.of("ZombieDefault", "ZombieArmor1", "ZombieArmor2", "ZombieArmor4"),
                    true);
            default -> throw new IllegalArgumentException("Vasebreaker stages are 1-3");
        };
    }

    public static MiniGameStageConfig placeholder(MiniGameId id) {
        return new MiniGameStageConfig(
                id, 1, 5, 9, 0, 0, 0, 0, 100, List.of(), List.of(), false);
    }
}
