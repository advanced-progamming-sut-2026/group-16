package io.github.finalwave.model.game.entity.zombie;

import io.github.finalwave.model.game.entity.Entity;
import io.github.finalwave.model.game.entity.GameContext;

import java.util.concurrent.atomic.AtomicLong;

public final class ArcadeObstacle extends Entity {

    public static final int BUCKET_EQUIVALENT_HEALTH = 1100;
    private static final AtomicLong NEXT_ID = new AtomicLong();

    private String pusherId;
    private int row;

    public ArcadeObstacle(Zombie pusher) {
        super("arcade-machine-" + NEXT_ID.incrementAndGet(), BUCKET_EQUIVALENT_HEALTH,
                Math.max(0, pusher.getX() - 0.45), pusher.getRow());
        this.pusherId = pusher.getId();
        this.row = pusher.getRow();
    }

    public String getPusherId() {
        return pusherId;
    }

    public int getRow() {
        return row;
    }

    public void follow(Zombie pusher) {
        if (pusher != null && pusher.getId().equals(pusherId) && pusher.isAlive()) {
            setX(Math.max(0, pusher.getX() - 0.45));
            row = pusher.getRow();
        }
    }

    public void releasePusher(String ownerId) {
        if (ownerId != null && ownerId.equals(pusherId)) {
            pusherId = null;
        }
    }

    public boolean blocksStraightProjectiles() {
        return true;
    }

    @Override
    public void onTickUpdate(GameContext context) {
        // Movement is driven by the living pusher's behavior.
    }
}
