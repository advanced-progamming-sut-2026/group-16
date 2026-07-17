package model.game.board.tile;

public class GraveTile extends Tile {

    public enum Loot {
        NONE,
        SUN_50,
        PLANT_FOOD
    }

    private final Loot loot;

    public GraveTile() {
        this(Loot.NONE);
    }

    public GraveTile(Loot loot) {
        this.loot = loot == null ? Loot.NONE : loot;
    }

    public Loot getLoot() {
        return loot;
    }

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
