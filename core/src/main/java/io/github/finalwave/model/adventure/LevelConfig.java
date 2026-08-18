package io.github.finalwave.model.adventure;

import java.util.List;

public final class LevelConfig {

    private final int index;
    private final LevelType type;
    private final int waveCount;
    private final int plantSlotCount;
    private final int startingSun;
    private final int baseWaveCost;
    private final List<String> allowedZombieAliases;
    private final String specialHandlerKey;

    public LevelConfig(int index,
                       LevelType type,
                       int waveCount,
                       int plantSlotCount,
                       int startingSun,
                       int baseWaveCost,
                       List<String> allowedZombieAliases,
                       String specialHandlerKey) {
        if (index < 1) {
            throw new IllegalArgumentException("level index must be >= 1");
        }
        this.index = index;
        this.type = type == null ? LevelType.NORMAL : type;
        this.waveCount = Math.max(1, waveCount);
        this.plantSlotCount = plantSlotCount <= 0 ? 8 : plantSlotCount;
        this.startingSun = Math.max(0, startingSun);
        this.baseWaveCost = Math.max(1, baseWaveCost);
        this.allowedZombieAliases = allowedZombieAliases == null
                ? List.of()
                : List.copyOf(allowedZombieAliases);
        this.specialHandlerKey = specialHandlerKey;
    }

    public int getIndex() {
        return index;
    }

    public LevelType getType() {
        return type;
    }

    public int getWaveCount() {
        return waveCount;
    }

    public int getPlantSlotCount() {
        return plantSlotCount;
    }

    public int getStartingSun() {
        return startingSun;
    }

    public int getBaseWaveCost() {
        return baseWaveCost;
    }

    public List<String> getAllowedZombieAliases() {
        return allowedZombieAliases;
    }

    public String getSpecialHandlerKey() {
        return specialHandlerKey;
    }

    public boolean isPlayableNow() {
        return type.isPlayableNow();
    }

    public static LevelConfig normal(int index, int waves, int startingSun, int baseWaveCost,
                                     List<String> zombies) {
        return new LevelConfig(index, LevelType.NORMAL, waves, 8, startingSun, baseWaveCost,
                zombies, null);
    }

    public static LevelConfig special(int index, LevelType type, String handlerKey) {
        return special(index, type, handlerKey, List.of("ZombieDefault"));
    }

    public static LevelConfig special(int index, LevelType type, String handlerKey, List<String> zombies) {
        return new LevelConfig(index, type, 3, 8, 50, 300, zombies, handlerKey);
    }

    public static LevelConfig boss(int index) {
        return new LevelConfig(index, LevelType.BOSS, 4, 8, 75, 500,
                List.of("ZombieDefault", "ZombieGargantuar"), "boss");
    }
}
