package model.game.board.tile;

import model.definition.plant.PlantDefinition;

public abstract class Tile {

    public boolean canPlant(PlantDefinition definition) {
        return !blocksPlanting();
    }

    public boolean blocksPlanting() {
        return false;
    }

    public boolean isWater() {
        return false;
    }

    public boolean blocksProjectiles() {
        return false;
    }

    public boolean isGrave() {
        return false;
    }

    public boolean isIce() {
        return false;
    }

    public boolean isCrater() {
        return false;
    }
}
