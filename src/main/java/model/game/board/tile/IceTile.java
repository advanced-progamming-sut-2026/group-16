package model.game.board.tile;

public class IceTile extends Tile {

    @Override
    public boolean blocksPlanting() {
        return true;
    }

    @Override
    public boolean isIce() {
        return true;
    }
}
