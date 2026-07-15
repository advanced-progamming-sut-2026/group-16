package model.game.board.tile;

public class GraveTile extends Tile {

    @Override
    public boolean blocksPlanting() {
        return true;
    }

    @Override
    public boolean isGrave() {
        return true;
    }

    @Override
    public boolean blocksProjectiles() {
        return true;
    }
}
