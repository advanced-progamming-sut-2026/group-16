package io.github.finalwave.model.collection;

public final class OwnedPlant {

    private final String plantName;
    private int level;
    private boolean unlocked;
    private int seedPackets;

    public OwnedPlant(String plantName) {
        this(plantName, 1, false, 0);
    }

    public OwnedPlant(String plantName, int level, boolean unlocked, int seedPackets) {
        if (plantName == null || plantName.isBlank()) {
            throw new IllegalArgumentException("plantName must not be blank");
        }
        validateLevel(level);
        if (seedPackets < 0) {
            throw new IllegalArgumentException("seedPackets must not be negative");
        }
        this.plantName = plantName;
        this.level = level;
        this.unlocked = unlocked;
        this.seedPackets = seedPackets;
    }

    public String getPlantName() {
        return plantName;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        validateLevel(level);
        this.level = level;
    }

    public boolean isUnlocked() {
        return unlocked;
    }

    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
    }

    public int getSeedPackets() {
        return seedPackets;
    }

    public void addSeedPackets(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("amount must not be negative");
        }
        seedPackets += amount;
    }

    public boolean consumeSeedPackets(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
        if (seedPackets < amount) {
            return false;
        }
        seedPackets -= amount;
        return true;
    }

    public OwnedPlant copy() {
        return new OwnedPlant(plantName, level, unlocked, seedPackets);
    }

    private static void validateLevel(int level) {
        if (level < 1 || level > 4) {
            throw new IllegalArgumentException("level must be between 1 and 4");
        }
    }
}
