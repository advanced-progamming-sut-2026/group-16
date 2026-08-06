package io.github.finalwave.model.game.entity;

public final class Vase extends Obstacle {

    public enum Content {EMPTY, ZOMBIE, PLANT_SEED, GARGANTUAR}

    private final Content content;
    private final String containedEntity;

    public Vase(String id, double x, double y, Content content, String containedEntity) {
        super(id, 300, x, y, false);
        this.content = content == null ? Content.EMPTY : content;
        this.containedEntity = containedEntity;
    }

    public Content getContent() {
        return content;
    }

    public String getContainedEntity() {
        return containedEntity;
    }

    public int getCol() {
        return (int) Math.floor(getX());
    }

    public int getRow() {
        return (int) Math.floor(getY());
    }

    public void smash(GameContext context) {
        if (isDead()) {
            return;
        }
        int col = getCol();
        int row = getRow();
        switch (content) {
            case ZOMBIE, GARGANTUAR -> {
                if (containedEntity != null && !containedEntity.isBlank() && context != null) {
                    context.spawnZombieOfType(containedEntity, row, col + 0.5);
                }
            }
            case PLANT_SEED -> {
                if (containedEntity != null && !containedEntity.isBlank() && context != null) {
                    context.dropSeedPacket(containedEntity, col, row);
                }
            }
            case EMPTY -> {
            }
        }
        takeDamage(getHealth());
    }

    @Override
    public void onTickUpdate(GameContext context) {
        // Vases are static
    }
}
