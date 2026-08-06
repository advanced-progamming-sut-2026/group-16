package io.github.finalwave.model.adventure;

public final class ChapterRules {

    private final boolean gravesAtStart;
    private final int startingGraveCount;
    private final boolean sandstormOnFinalWave;
    private final int sandstormMinOffset;
    private final int sandstormMaxOffset;
    private final boolean zombiesImmuneToChill;
    private final boolean iceWindEnabled;
    private final boolean slipperyTilesEnabled;
    private final boolean preFrozenZombies;
    private final boolean waterColumns;
    private final int initialWaterColumns;
    private final int maxTideColumn;
    private final boolean skySunEnabled;
    private final boolean gravesOnWaveStart;
    private final boolean necromancyTiles;
    private final boolean graveLootEnabled;
    private final boolean lowBeachEmerge;

    private ChapterRules(Builder builder) {
        this.gravesAtStart = builder.gravesAtStart;
        this.startingGraveCount = builder.startingGraveCount;
        this.sandstormOnFinalWave = builder.sandstormOnFinalWave;
        this.sandstormMinOffset = builder.sandstormMinOffset;
        this.sandstormMaxOffset = builder.sandstormMaxOffset;
        this.zombiesImmuneToChill = builder.zombiesImmuneToChill;
        this.iceWindEnabled = builder.iceWindEnabled;
        this.slipperyTilesEnabled = builder.slipperyTilesEnabled;
        this.preFrozenZombies = builder.preFrozenZombies;
        this.waterColumns = builder.waterColumns;
        this.initialWaterColumns = builder.initialWaterColumns;
        this.maxTideColumn = builder.maxTideColumn;
        this.skySunEnabled = builder.skySunEnabled;
        this.gravesOnWaveStart = builder.gravesOnWaveStart;
        this.necromancyTiles = builder.necromancyTiles;
        this.graveLootEnabled = builder.graveLootEnabled;
        this.lowBeachEmerge = builder.lowBeachEmerge;
    }

    public boolean hasGravesAtStart() {
        return gravesAtStart;
    }

    public int getStartingGraveCount() {
        return startingGraveCount;
    }

    public boolean hasSandstormOnFinalWave() {
        return sandstormOnFinalWave;
    }

    public int getSandstormMinOffset() {
        return sandstormMinOffset;
    }

    public int getSandstormMaxOffset() {
        return sandstormMaxOffset;
    }

    public boolean areZombiesImmuneToChill() {
        return zombiesImmuneToChill;
    }

    public boolean isIceWindEnabled() {
        return iceWindEnabled;
    }

    public boolean hasSlipperyTiles() {
        return slipperyTilesEnabled;
    }

    public boolean hasPreFrozenZombies() {
        return preFrozenZombies;
    }

    public boolean hasWaterColumns() {
        return waterColumns;
    }

    public int getInitialWaterColumns() {
        return initialWaterColumns;
    }

    public int getMaxTideColumn() {
        return maxTideColumn;
    }

    public boolean isSkySunEnabled() {
        return skySunEnabled;
    }

    public boolean hasGravesOnWaveStart() {
        return gravesOnWaveStart;
    }

    public boolean hasNecromancyTiles() {
        return necromancyTiles;
    }

    public boolean hasGraveLoot() {
        return graveLootEnabled;
    }

    public boolean hasLowBeachEmerge() {
        return lowBeachEmerge;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static ChapterRules ancientEgypt() {
        return builder()
                .gravesAtStart(true)
                .startingGraveCount(4)
                .sandstormOnFinalWave(true)
                .sandstormOffset(1, 4)
                .skySunEnabled(true)
                .build();
    }

    public static ChapterRules frostbiteCaves() {
        return builder()
                .zombiesImmuneToChill(true)
                .iceWindEnabled(true)
                .slipperyTilesEnabled(true)
                .preFrozenZombies(true)
                .skySunEnabled(true)
                .build();
    }

    public static ChapterRules bigWaveBeach() {
        return builder()
                .waterColumns(true)
                .initialWaterColumns(3)
                .maxTideColumn(5)
                .lowBeachEmerge(true)
                .skySunEnabled(true)
                .build();
    }

    public static ChapterRules darkAges() {
        return builder()
                .gravesAtStart(true)
                .startingGraveCount(3)
                .gravesOnWaveStart(true)
                .necromancyTiles(true)
                .graveLootEnabled(true)
                .skySunEnabled(false)
                .build();
    }

    public static final class Builder {
        private boolean gravesAtStart;
        private int startingGraveCount;
        private boolean sandstormOnFinalWave;
        private int sandstormMinOffset = 1;
        private int sandstormMaxOffset = 4;
        private boolean zombiesImmuneToChill;
        private boolean iceWindEnabled;
        private boolean slipperyTilesEnabled;
        private boolean preFrozenZombies;
        private boolean waterColumns;
        private int initialWaterColumns = 3;
        private int maxTideColumn = 5;
        private boolean skySunEnabled = true;
        private boolean gravesOnWaveStart;
        private boolean necromancyTiles;
        private boolean graveLootEnabled;
        private boolean lowBeachEmerge;

        public Builder gravesAtStart(boolean value) {
            this.gravesAtStart = value;
            return this;
        }

        public Builder startingGraveCount(int count) {
            this.startingGraveCount = Math.max(0, count);
            return this;
        }

        public Builder sandstormOnFinalWave(boolean value) {
            this.sandstormOnFinalWave = value;
            return this;
        }

        public Builder sandstormOffset(int min, int max) {
            this.sandstormMinOffset = min;
            this.sandstormMaxOffset = max;
            return this;
        }

        public Builder zombiesImmuneToChill(boolean value) {
            this.zombiesImmuneToChill = value;
            return this;
        }

        public Builder iceWindEnabled(boolean value) {
            this.iceWindEnabled = value;
            return this;
        }

        public Builder slipperyTilesEnabled(boolean value) {
            this.slipperyTilesEnabled = value;
            return this;
        }

        public Builder preFrozenZombies(boolean value) {
            this.preFrozenZombies = value;
            return this;
        }

        public Builder waterColumns(boolean value) {
            this.waterColumns = value;
            return this;
        }

        public Builder initialWaterColumns(int columns) {
            this.initialWaterColumns = columns;
            return this;
        }

        public Builder maxTideColumn(int column) {
            this.maxTideColumn = column;
            return this;
        }

        public Builder skySunEnabled(boolean value) {
            this.skySunEnabled = value;
            return this;
        }

        public Builder gravesOnWaveStart(boolean value) {
            this.gravesOnWaveStart = value;
            return this;
        }

        public Builder necromancyTiles(boolean value) {
            this.necromancyTiles = value;
            return this;
        }

        public Builder graveLootEnabled(boolean value) {
            this.graveLootEnabled = value;
            return this;
        }

        public Builder lowBeachEmerge(boolean value) {
            this.lowBeachEmerge = value;
            return this;
        }

        public ChapterRules build() {
            return new ChapterRules(this);
        }
    }
}
