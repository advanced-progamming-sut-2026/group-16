package model.game.entity;

public abstract class Plant extends Entity {

    private final String name;
    private final int level;

    protected Plant(String id, String name, int level, int maxHealth, int col, int row) {
        super(id, maxHealth, col, row);
        this.name = name;
        this.level = level;
    }

    public String getName() {
        return name;
    }

    public int getLevel() {
        return level;
    }

    public int getCol() {
        return (int) getX();
    }

    public int getRow() {
        return (int) getY();
    }

    public abstract void activatePlantFoodEffect(GameContext context);

    @Override
    protected void onDeath() {
        // GameContext will detect dead plants via isDead() and clean up
    }
}