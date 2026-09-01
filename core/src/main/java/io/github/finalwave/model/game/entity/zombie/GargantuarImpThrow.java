package io.github.finalwave.model.game.entity.zombie;

public final class GargantuarImpThrow {

    private final String impAlias;
    private final int landColumn;
    private final double spawnOffsetX;
    private final double spawnForwardTiles;
    private final double spawnLift;
    private final double arcApex;
    private final int flyTicks;
    private final int landTicks;
    private final int throwHoldTicks;
    private final int releaseTicksAfterStart;

    public GargantuarImpThrow(
            String impAlias,
            int landColumn,
            double spawnOffsetX,
            double spawnForwardTiles,
            double spawnLift,
            double arcApex,
            int flyTicks,
            int landTicks,
            int throwHoldTicks,
            int releaseTicksAfterStart) {
        this.impAlias = impAlias;
        this.landColumn = landColumn;
        this.spawnOffsetX = spawnOffsetX;
        this.spawnForwardTiles = spawnForwardTiles;
        this.spawnLift = spawnLift;
        this.arcApex = arcApex;
        this.flyTicks = flyTicks;
        this.landTicks = landTicks;
        this.throwHoldTicks = throwHoldTicks;
        this.releaseTicksAfterStart = releaseTicksAfterStart;
    }

    public String impAlias() {
        return impAlias;
    }

    public int landColumn() {
        return landColumn;
    }

    public double spawnOffsetX() {
        return spawnOffsetX;
    }

    public double spawnForwardTiles() {
        return spawnForwardTiles;
    }

    public double spawnLift() {
        return spawnLift;
    }

    public double arcApex() {
        return arcApex;
    }

    public int flyTicks() {
        return flyTicks;
    }

    public int landTicks() {
        return landTicks;
    }

    public int releaseTicksAfterStart() {
        return releaseTicksAfterStart;
    }

    public int throwHoldTicks() {
        return throwHoldTicks;
    }

    public double landX() {
        return landColumn + 0.5;
    }
}
