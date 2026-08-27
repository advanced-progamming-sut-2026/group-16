package io.github.finalwave.model.item;

public final class Sun {

    public static final int FALL_TICKS = 50;
    public static final int GROUND_LIFETIME_TICKS = Integer.MAX_VALUE / 4;

    private final int col;
    private final int row;
    private int value;
    private SunType type;
    private int lifetimeTicks;
    private int fallTicksRemaining;
    private boolean fromPlant;
    private boolean reachedGround;
    private String attractZombieId;
    private boolean consumed;
    private int attractTicksRemaining;

    public Sun(int col, int row, int value, SunType type, boolean fromPlant) {
        this.col = col;
        this.row = row;
        this.value = value;
        this.type = type == null ? SunType.NORMAL : type;
        this.fromPlant = fromPlant;
        if (fromPlant) {
            this.lifetimeTicks = Integer.MAX_VALUE / 2;
            this.fallTicksRemaining = 0;
            this.reachedGround = true;
        } else {
            this.lifetimeTicks = GROUND_LIFETIME_TICKS;
            this.fallTicksRemaining = FALL_TICKS;
            this.reachedGround = false;
        }
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

    public void setType(SunType type) {
        this.type = type == null ? SunType.NORMAL : type;
    }

    public boolean isFromPlant() {
        return fromPlant;
    }

    public int getLifetimeTicks() {
        return lifetimeTicks;
    }

    public boolean isFalling() {
        return !fromPlant && !reachedGround && fallTicksRemaining > 0;
    }

    public boolean hasReachedGround() {
        return reachedGround;
    }

    public int getFallTicksRemaining() {
        return fallTicksRemaining;
    }

    public boolean tick() {
        if (attractTicksRemaining > 0) {
            attractTicksRemaining--;
            if (attractTicksRemaining == 0) {
                finishAttract();
            }
        }
        if (consumed) {
            return false;
        }
        if (fromPlant) {
            return false;
        }
        if (!reachedGround) {
            fallTicksRemaining--;
            if (fallTicksRemaining <= 0) {
                reachedGround = true;
                if (type == SunType.RADIOACTIVE) {
                    type = SunType.NORMAL;
                    value = 25;
                }
                lifetimeTicks = GROUND_LIFETIME_TICKS;
                return true;
            }
            return false;
        }
        if (lifetimeTicks < Integer.MAX_VALUE / 4) {
            lifetimeTicks--;
        }
        return false;
    }

    public boolean isExpired() {
        if (consumed) {
            return true;
        }
        if (attractZombieId != null) {
            return false;
        }
        return !fromPlant && reachedGround && lifetimeTicks <= 0;
    }

    public void attractTo(String zombieId) {
        if (zombieId != null && !zombieId.isBlank()) {
            attractZombieId = zombieId;
            attractTicksRemaining = Math.max(attractTicksRemaining, 12);
        }
    }

    public String attractZombieId() {
        return attractZombieId;
    }

    public boolean isAttracted() {
        return attractZombieId != null && !consumed;
    }

    public void finishAttract() {
        attractZombieId = null;
        attractTicksRemaining = 0;
        consumed = true;
        value = 0;
        lifetimeTicks = 0;
        reachedGround = true;
    }
}
