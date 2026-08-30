package io.github.finalwave.network.sync;

public final class UpdateGreenhousePotPayload {
    private int x;
    private int y;
    private boolean locked;
    private String plantType;
    private long plantedAtMillis;
    private boolean marigold;

    public UpdateGreenhousePotPayload() {
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public String getPlantType() {
        return plantType;
    }

    public void setPlantType(String plantType) {
        this.plantType = plantType;
    }

    public long getPlantedAtMillis() {
        return plantedAtMillis;
    }

    public void setPlantedAtMillis(long plantedAtMillis) {
        this.plantedAtMillis = plantedAtMillis;
    }

    public boolean isMarigold() {
        return marigold;
    }

    public void setMarigold(boolean marigold) {
        this.marigold = marigold;
    }
}
