package model.item;

public final class Sun {

    private final int col;
    private final int row;
    private int value;
    private final SunType type;
    private int lifetimeTicks;
    private final boolean fromPlant;

    public Sun(int col, int row, int value, SunType type, boolean fromPlant) {
        this.col = col;
        this.row = row;
        this.value = value;
        this.type = type == null ? SunType.NORMAL : type;
        this.fromPlant = fromPlant;
        this.lifetimeTicks = fromPlant ? Integer.MAX_VALUE / 2 : 50;
    }

    public int getCol() {
        return col;
    }

    public int getRow() {
        return row;
    }

    public int getValue() {
        return value;
    }

    public int takeValue(int maximum) {
        int taken = Math.min(value, Math.max(0, maximum));
        value -= taken;
        return taken;
    }

    public SunType getType() {
        return type;
    }

    public boolean isFromPlant() {
        return fromPlant;
    }

    public int getLifetimeTicks() {
        return lifetimeTicks;
    }

    public void tick() {
        if (!fromPlant && lifetimeTicks < Integer.MAX_VALUE / 4) {
            lifetimeTicks--;
        }
    }

    public boolean isExpired() {
        return !fromPlant && lifetimeTicks <= 0;
    }
}
