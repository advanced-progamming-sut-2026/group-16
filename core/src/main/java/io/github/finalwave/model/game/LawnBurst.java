package io.github.finalwave.model.game;

public record LawnBurst(Kind kind, int col, int row, int span, float originX, float originY) {

    public LawnBurst(Kind kind, int col, int row) {
        this(kind, col, row, 1, col + 0.5f, row);
    }

    public LawnBurst(Kind kind, int col, int row, int span) {
        this(kind, col, row, span, col + 0.5f, row);
    }

    public LawnBurst(Kind kind, int col, int row, int span, double originX) {
        this(kind, col, row, span, (float) originX, row);
    }

    public enum Kind {
        CHERRY,
        MINE,
        GENERIC,
        BURN,
        BONE_HIT,
        LASER
    }

    public static Kind kindForPlant(String plantName) {
        if (plantName == null) {
            return Kind.GENERIC;
        }
        String lower = plantName.toLowerCase();
        if (lower.contains("potato") || lower.contains("mine")) {
            return Kind.MINE;
        }
        return Kind.CHERRY;
    }
}
