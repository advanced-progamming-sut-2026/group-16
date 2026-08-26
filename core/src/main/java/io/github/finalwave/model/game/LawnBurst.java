package io.github.finalwave.model.game;

public record LawnBurst(Kind kind, int col, int row) {

    public enum Kind {
        CHERRY,
        MINE,
        GENERIC
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
