package io.github.finalwave.model.game;

public final class SeedPlacement {

    private final String plantName;
    private final int col;
    private final int row;

    public SeedPlacement(String plantName, int col, int row) {
        if (plantName == null || plantName.isBlank()) {
            throw new IllegalArgumentException("plantName must not be blank");
        }
        this.plantName = plantName;
        this.col = col;
        this.row = row;
    }

    public String getPlantName() {
        return plantName;
    }

    public int getCol() {
        return col;
    }

    public int getRow() {
        return row;
    }
}
