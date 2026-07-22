package model.minigame;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
    private final int redLineColumn;
    private final int waveCount;
    private final int baseWaveCost;
    private final List<String> conveyorPlantPool;
    private final Map<String, Integer> zombieSunCosts;
    private final int prePlantedPlantCount;

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
        this(miniGameId, stageIndex, rows, cols, startingSun, potCount, plantSeedPotCount,
                gargantuarPotCount, seedPacketExpiryTicks, plantSeedPool, zombiePool, implemented,
                -1, 1, 100, List.of(), Map.of(), 0);
    }

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
                               boolean implemented,
                               int redLineColumn,
                               int waveCount,
                               int baseWaveCost,
                               List<String> conveyorPlantPool) {
        this(miniGameId, stageIndex, rows, cols, startingSun, potCount, plantSeedPotCount,
                gargantuarPotCount, seedPacketExpiryTicks, plantSeedPool, zombiePool, implemented,
                redLineColumn, waveCount, baseWaveCost, conveyorPlantPool, Map.of(), 0);
    }

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
                               boolean implemented,
                               int redLineColumn,
                               int waveCount,
                               int baseWaveCost,
                               List<String> conveyorPlantPool,
                               Map<String, Integer> zombieSunCosts,
                               int prePlantedPlantCount) {
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
        this.redLineColumn = redLineColumn;
        this.waveCount = Math.max(1, waveCount);
        this.baseWaveCost = Math.max(1, baseWaveCost);
        this.conveyorPlantPool = conveyorPlantPool == null ? List.of() : List.copyOf(conveyorPlantPool);
        this.zombieSunCosts = zombieSunCosts == null
                ? Map.of()
                : Map.copyOf(zombieSunCosts);
        this.prePlantedPlantCount = Math.max(0, prePlantedPlantCount);
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

    public int getRedLineColumn() {
        return redLineColumn;
    }

    public int getWaveCount() {
        return waveCount;
    }

    public int getBaseWaveCost() {
        return baseWaveCost;
    }

    public List<String> getConveyorPlantPool() {
        return conveyorPlantPool;
    }

    public Map<String, Integer> getZombieSunCosts() {
        return zombieSunCosts;
    }

    public int getPrePlantedPlantCount() {
        return prePlantedPlantCount;
    }

    public int getCheapestZombieCost() {
        if (zombieSunCosts.isEmpty()) {
            return Integer.MAX_VALUE;
        }
        int cheapest = Integer.MAX_VALUE;
        for (int cost : zombieSunCosts.values()) {
            cheapest = Math.min(cheapest, cost);
        }
        return cheapest;
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

    public static MiniGameStageConfig walnutBowling(int stageIndex) {
        List<String> nuts = List.of("Wall-nut", "Explode-o-nut", "Giant Wall-nut");
        return switch (stageIndex) {
            case 1 -> new MiniGameStageConfig(
                    MiniGameId.WALNUT_BOWLING, 1, 5, 9, 0,
                    0, 0, 0, 100, List.of(), List.of("ZombieDefault"),
                    true, 4, 2, 100, nuts);
            case 2 -> new MiniGameStageConfig(
                    MiniGameId.WALNUT_BOWLING, 2, 5, 9, 0,
                    0, 0, 0, 100, List.of(),
                    List.of("ZombieDefault", "ZombieArmor1"),
                    true, 4, 3, 120, nuts);
            case 3 -> new MiniGameStageConfig(
                    MiniGameId.WALNUT_BOWLING, 3, 5, 9, 0,
                    0, 0, 0, 100, List.of(),
                    List.of("ZombieDefault", "ZombieArmor1", "ZombieArmor2"),
                    true, 3, 4, 140, nuts);
            default -> throw new IllegalArgumentException("Walnut bowling stages are 1-3");
        };
    }

    public static MiniGameStageConfig iZombie(int stageIndex) {
        return switch (stageIndex) {
            case 1 -> new MiniGameStageConfig(
                    MiniGameId.I_ZOMBIE, 1, 5, 9, 150,
                    0, 0, 0, 100,
                    List.of("Peashooter", "Sunflower", "Wall-nut"),
                    List.of("ZombieDefault", "ZombieImp", "ZombieArmor1",
                            "ZombieNewspaper", "ZombieBeachFisherman"),
                    true, 2, 1, 100, List.of(),
                    costs(
                            "ZombieDefault", 50,
                            "ZombieImp", 25,
                            "ZombieArmor1", 75,
                            "ZombieNewspaper", 75,
                            "ZombieBeachFisherman", 100),
                    6);
            case 2 -> new MiniGameStageConfig(
                    MiniGameId.I_ZOMBIE, 2, 5, 9, 150,
                    0, 0, 0, 100,
                    List.of("Peashooter", "Sunflower", "Wall-nut", "Repeater", "Potato Mine"),
                    List.of("ZombieImp", "ZombieArmor1", "ZombieArmor4",
                            "ZombieDarkArmor3", "ZombiePiano"),
                    true, 3, 1, 100, List.of(),
                    costs(
                            "ZombieImp", 25,
                            "ZombieArmor1", 75,
                            "ZombieArmor4", 125,
                            "ZombieDarkArmor3", 150,
                            "ZombiePiano", 125),
                    9);
            case 3 -> new MiniGameStageConfig(
                    MiniGameId.I_ZOMBIE, 3, 5, 9, 150,
                    0, 0, 0, 100,
                    List.of("Peashooter", "Repeater", "Wall-nut", "Tall-nut", "Threepeater"),
                    List.of("ZombieArmor4", "ZombieDarkArmor3", "ZombiePiano",
                            "ZombieBeachOctopus", "ZombieGargantuar"),
                    true, 3, 1, 100, List.of(),
                    costs(
                            "ZombieArmor4", 125,
                            "ZombieDarkArmor3", 150,
                            "ZombiePiano", 125,
                            "ZombieBeachOctopus", 150,
                            "ZombieGargantuar", 300),
                    12);
            default -> throw new IllegalArgumentException("I, Zombie stages are 1-3");
        };
    }

    private static Map<String, Integer> costs(Object... pairs) {
        Map<String, Integer> map = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            map.put((String) pairs[i], (Integer) pairs[i + 1]);
        }
        return map;
    }

    public static MiniGameStageConfig placeholder(MiniGameId id) {
        return new MiniGameStageConfig(
                id, 1, 5, 9, 0, 0, 0, 0, 100, List.of(), List.of(), false);
    }
}
