package io.github.finalwave.model.game.entity.projectile;

import io.github.finalwave.model.game.GameSession;


public final class JalapenoMuzzles {

    public static final float IDLE_SECONDS = 0.6667f;
    public static final float ATTACK_SECONDS = 0.6667f;
    public static final int PROPAGATION_DELAY_TICKS = 1;

    public static final float FIRE_IDLE_SECONDS = 0.6667f;
    public static final float FIRE_IDLE2_SECONDS = 1.3333f;
    public static final float FIRE_IDLE3_SECONDS = 0.6667f;

    private JalapenoMuzzles() {
    }

    public static int idleTicks() {
        return secondsToTicks(IDLE_SECONDS);
    }

    public static int attackTicks() {
        return secondsToTicks(ATTACK_SECONDS);
    }

    private static int secondsToTicks(float seconds) {
        return Math.max(1, Math.round(seconds * GameSession.TICKS_PER_SECOND));
    }
}
