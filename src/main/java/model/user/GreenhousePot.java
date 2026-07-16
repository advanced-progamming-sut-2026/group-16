package model.user;

public class GreenhousePot {
    public static final String MARIGOLD = "Marigold";

    private final int x;
    private final int y;
    private boolean locked;
    private String plantType;
    private long plantedAtMillis;
    private boolean marigold;

    public GreenhousePot(int x, int y, boolean locked) {
        this.x = x;
        this.y = y;
        this.locked = locked;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
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

    public long getPlantedAtMillis() {
        return plantedAtMillis;
    }

    public boolean isMarigold() {
        return marigold;
    }

    public boolean isEmpty() {
        return plantType == null;
    }

    public void plant(String plantType, boolean marigold, long plantedAtMillis) {
        this.plantType = plantType;
        this.marigold = marigold;
        this.plantedAtMillis = plantedAtMillis;
    }

    public void clear() {
        this.plantType = null;
        this.marigold = false;
        this.plantedAtMillis = 0L;
    }
}
