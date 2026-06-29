package model.game.entity;

public final class Vase extends Obstacle {

    public enum Content {EMPTY, ZOMBIE, PLANT_SEED, GARGANTUAR}

    private final Content content;
    private final String containedEntity;

    public Vase(String id, double x, double y, Content content, String containedEntity) {
        super(id, 300, x, y, false);
        this.content = content;
        this.containedEntity = containedEntity;
    }

    public Content getContent() {
        return content;
    }

    public String getContainedEntity() {
        return containedEntity;
    }

    public void smash(GameContext context) {
        // TODO: spawn contained entity via context
        takeDamage(getHealth());
    }

    @Override
    public void onTickUpdate(GameContext context) {
        // Vases are static
    }
}