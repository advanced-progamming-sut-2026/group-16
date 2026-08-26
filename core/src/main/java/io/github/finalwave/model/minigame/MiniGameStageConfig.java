package io.github.finalwave.model.minigame;

import io.github.finalwave.model.minigame.beghouled.BeghouledUpgradeCatalog;
import io.github.finalwave.model.minigame.beghouled.BeghouledUpgradeRule;

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
    private final int matchTarget;
    private final List<BeghouledUpgradeRule> upgrades;

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
        this(miniGameId, stageIndex, rows, cols, startingSun, potCount, plantSeedPotCount,
                gargantuarPotCount, seedPacketExpiryTicks, plantSeedPool, zombiePool, implemented,
                redLineColumn, waveCount, baseWaveCost, conveyorPlantPool, zombieSunCosts,
                prePlantedPlantCount, 0, List.of());
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
                               int prePlantedPlantCount,
                               int matchTarget,
                               List<BeghouledUpgradeRule> upgrades) {
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
        this.matchTarget = Math.max(0, matchTarget);
        this.upgrades = upgrades == null ? List.of() : List.copyOf(upgrades);
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

    public int getMatchTarget() {
        return matchTarget;
    }

    public List<BeghouledUpgradeRule> getUpgrades() {
        return upgrades;
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

    public static MiniGameStageConfig beghouled(int stageIndex) {
        return switch (stageIndex) {
            case 1 -> new MiniGameStageConfig(
                    MiniGameId.BEGHOULED, 1, 5, 9, 0,
                    0, 0, 0, 100,
                    List.of("Peashooter", "Sunflower", "Wall-nut", "Puff-shroom", "Cabbage-pult"),
                    List.of("ZombieDefault"),
                    true, -1, 500, 80, List.of(), Map.of(), 0,
                    8, BeghouledUpgradeCatalog.stageOne().getRules());
            case 2 -> new MiniGameStageConfig(
                    MiniGameId.BEGHOULED, 2, 5, 9, 0,
                    0, 0, 0, 100,
                    List.of("Sunflower", "Repeater", "Wall-nut", "Fume-shroom", "Melon-pult"),
                    List.of("ZombieDefault", "ZombieArmor1"),
                    true, -1, 500, 100, List.of(), Map.of(), 0,
                    12, BeghouledUpgradeCatalog.stageTwo().getRules());
            case 3 -> new MiniGameStageConfig(
                    MiniGameId.BEGHOULED, 3, 5, 9, 0,
                    0, 0, 0, 100,
                    List.of("Peashooter", "Repeater", "Tall-nut", "Cabbage-pult", "Melon-pult"),
                    List.of("ZombieDefault", "ZombieArmor1", "ZombieArmor2"),
                    true, -1, 500, 130, List.of(), Map.of(), 0,
                    16, BeghouledUpgradeCatalog.stageThree().getRules());
            default -> throw new IllegalArgumentException("Beghouled stages are 1-3");
        };
    }

    public static MiniGameStageConfig zombotany(int stageIndex) {
        return switch (stageIndex) {
            case 1 -> new MiniGameStageConfig(
                    MiniGameId.ZOMBOTANY, 1, 5, 9, 50,
                    0, 0, 0, 100,
                    List.of("Peashooter", "Sunflower", "Wall-nut", "Potato Mine", "Cabbage-pult"),
                    List.of("ZombiePeaShooter", "ZombiePeaShooter", "ZombiePeaShooter",
                            "ZombiePeaShooter", "ZombieDefault"),
                    true, -1, 2, 250, List.of());
            case 2 -> new MiniGameStageConfig(
                    MiniGameId.ZOMBOTANY, 2, 5, 9, 50,
                    0, 0, 0, 100,
                    List.of("Peashooter", "Sunflower", "Wall-nut", "Repeater", "Potato Mine", "Cabbage-pult"),
                    List.of("ZombiePeaShooter", "ZombiePeaShooter", "ZombieWallNut", "ZombieWallNut",
                            "ZombiePeaShooter", "ZombieDefault"),
                    true, -1, 3, 300, List.of());
            case 3 -> new MiniGameStageConfig(
                    MiniGameId.ZOMBOTANY, 3, 5, 9, 75,
                    0, 0, 0, 100,
                    List.of("Peashooter", "Sunflower", "Repeater", "Tall-nut", "Potato Mine", "Melon-pult"),
                    List.of("ZombiePeaShooter", "ZombieWallNut", "ZombieJalapeno", "ZombieSquash",
                            "ZombiePeaShooter", "ZombieJalapeno", "ZombieSquash", "ZombieWallNut",
                            "ZombieDefault"),
                    true, -1, 4, 350, List.of());
            default -> throw new IllegalArgumentException("Zombotany stages are 1-3");
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
