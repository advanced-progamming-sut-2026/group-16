package io.github.finalwave.network.sync;

public final class UpdatePlantPayload {
    private String plantName;
    private int level;
    private boolean unlocked;
    private int seedPackets;

    public UpdatePlantPayload() {
    }

    public UpdatePlantPayload(String plantName, int level, boolean unlocked, int seedPackets) {
        this.plantName = plantName;
        this.level = level;
        this.unlocked = unlocked;
        this.seedPackets = seedPackets;
    }

    public String getPlantName() {
        return plantName;
    }

    public void setPlantName(String plantName) {
        this.plantName = plantName;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
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

    public void setSeedPackets(int seedPackets) {
        this.seedPackets = seedPackets;
    }
}
