package io.github.finalwave.model.game.entity.zombie;

import io.github.finalwave.model.game.entity.Entity;
import io.github.finalwave.model.game.entity.GameContext;

import java.util.concurrent.atomic.AtomicLong;

public final class PianoObstacle extends Entity {

    public static final int BUCKET_EQUIVALENT_HEALTH = 1100;
    public static final double FOLLOW_OFFSET = 0.28;
    private static final AtomicLong NEXT_ID = new AtomicLong();

    private String pusherId;
    private int row;
    private boolean playing;

    public PianoObstacle(Zombie pusher) {
        super("piano-" + NEXT_ID.incrementAndGet(), BUCKET_EQUIVALENT_HEALTH,
                followX(pusher), pusher.getRow());
        this.pusherId = pusher.getId();
        this.row = pusher.getRow();
        this.playing = "play".equals(pusher.getPresentationClip())
                || "play2".equals(pusher.getPresentationClip());
    }

    public String getPusherId() {
        return pusherId;
    }

    public int getRow() {
        return row;
    }

    public boolean isPlaying() {
        return playing;
    }

    public void follow(Zombie pusher) {
        if (pusher != null && pusher.getId().equals(pusherId) && pusher.isAlive()) {
            setX(followX(pusher));
            row = pusher.getRow();
            playing = "play".equals(pusher.getPresentationClip())
                    || "play2".equals(pusher.getPresentationClip());
        }
    }

    public void releasePusher(String ownerId) {
        if (ownerId != null && ownerId.equals(pusherId)) {
            pusherId = null;
            playing = false;
        }
    }

    public boolean blocksStraightProjectiles() {
        return true;
    }

    @Override
    public void onTickUpdate(GameContext context) {
    }

    private static double followX(Zombie pusher) {
        return Math.max(0, pusher.getX() - FOLLOW_OFFSET);
    }
}
