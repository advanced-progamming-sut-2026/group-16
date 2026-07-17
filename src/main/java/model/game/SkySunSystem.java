package model.game;

import model.item.Sun;
import model.item.SunType;

import java.util.Random;

public final class SkySunSystem {

    private static final int FALL_TICKS = Sun.FALL_TICKS;

    private final Random random;
    private boolean enabled = true;
    private double difficultyScale = 1.0;
    private int ticksUntilNextDrop;
    private boolean initialized;

    public SkySunSystem(Random random) {
        this.random = random == null ? new Random() : random;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setDifficultyScale(double scale) {
        this.difficultyScale = Math.max(0.1, scale);
    }

    public double getDifficultyScale() {
        return difficultyScale;
    }

    public int getTicksUntilNextDrop() {
        return ticksUntilNextDrop;
    }

    public static double intervalSeconds(double elapsedSeconds) {
        return Math.max(6.0 + 0.05 * elapsedSeconds, 12.0);
    }

    public static int intervalTicks(double elapsedSeconds, int ticksPerSecond, double difficultyScale) {
        double seconds = intervalSeconds(elapsedSeconds) * Math.max(0.1, difficultyScale);
        return Math.max(1, (int) Math.round(seconds * ticksPerSecond));
    }

    public SunType rollSunType() {
        int roll = random.nextInt(100);
        if (roll < 80) {
            return SunType.NORMAL;
        }
        if (roll < 95) {
            return SunType.SPECIAL;
        }
        return SunType.RADIOACTIVE;
    }

    public static int valueFor(SunType type) {
        return switch (type) {
            case NORMAL -> 25;
            case SPECIAL -> 100;
            case RADIOACTIVE -> 25;
        };
    }

    public Sun tick(int currentTick, int ticksPerSecond, int cols, int rows) {
        if (!enabled || cols <= 0 || rows <= 0) {
            return null;
        }
        if (!initialized) {
            double elapsed = currentTick / (double) ticksPerSecond;
            ticksUntilNextDrop = intervalTicks(elapsed, ticksPerSecond, difficultyScale);
            initialized = true;
        }
        ticksUntilNextDrop--;
        if (ticksUntilNextDrop > 0) {
            return null;
        }
        double elapsed = currentTick / (double) ticksPerSecond;
        ticksUntilNextDrop = intervalTicks(elapsed, ticksPerSecond, difficultyScale);
        int col = random.nextInt(cols);
        int row = random.nextInt(rows);
        SunType type = rollSunType();
        return new Sun(col, row, valueFor(type), type, false);
    }

    public static int fallDurationTicks() {
        return FALL_TICKS;
    }
}
