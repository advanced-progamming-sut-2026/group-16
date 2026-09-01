package io.github.finalwave.model.item;


public final class PlantFoodDrop {
    private final int col;
    private final int row;
    private final double worldX;
    private boolean consumed;

    public PlantFoodDrop(int col, int row, double worldX) {
        this.col = col;
        this.row = row;
        this.worldX = worldX;
    }

    public int getCol() {
        return col;
    }

    public int getRow() {
        return row;
    }

    public double getWorldX() {
        return worldX;
    }

    public boolean isConsumed() {
        return consumed;
    }

    public void consume() {
        consumed = true;
    }
}
