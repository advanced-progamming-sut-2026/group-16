package io.github.finalwave.model.game.entity.projectile;

import io.github.finalwave.model.game.GameSession;


public final class GrapeshotMuzzles {

    public static final float IDLE_SECONDS = 4.0f;
    public static final float ATTACK_SPAWN_SECONDS = 0.77f;
    public static final int GRAPE_COUNT = 7;
    public static final float GRAPE_SPEED_TILES_PER_TICK = 0.45f;
    public static final float GRAPE_JITTER_RADIANS = 0.08f;
    public static final double GRAPE_HIT_RADIUS = 0.35;
    public static final float GRAPE_LIFETIME_SECONDS = 6.0f;

    private GrapeshotMuzzles() {
    }

    public static int grapeLifetimeTicks() {
        return secondsToTicks(GRAPE_LIFETIME_SECONDS);
    }

    public static int idleTicks() {
        return secondsToTicks(IDLE_SECONDS);
    }

    public static int attackSpawnTicks() {
        return secondsToTicks(ATTACK_SPAWN_SECONDS);
    }

    private static int secondsToTicks(float seconds) {
        return Math.max(1, Math.round(seconds * GameSession.TICKS_PER_SECOND));
    }
}
