package io.github.finalwave.model.game.board.tile;

public class CraterTile extends Tile {

    @Override
    public boolean blocksPlanting() {
        return true;
    }

    @Override
    public boolean isCrater() {
        return true;
    }
}
