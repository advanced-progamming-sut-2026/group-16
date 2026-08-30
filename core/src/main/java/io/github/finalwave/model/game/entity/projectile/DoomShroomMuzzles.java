package io.github.finalwave.model.game.entity.projectile;

import io.github.finalwave.model.game.GameSession;


public final class DoomShroomMuzzles {

    public static final float STAGE1_TO_STAGE2_SECONDS = 8.0f;
    public static final float STAGE2_TO_STAGE3_SECONDS = 12.0f;
    public static final float CRATER_DURATION_SECONDS = 13.5f;
    public static final int PROXIMITY_ALERT_TILES = 2;

    public static final int STAGE1_DAMAGE = 1100;
    public static final int STAGE2_DAMAGE = 1400;
    public static final int STAGE3_DAMAGE = 2200;

    public static final int STAGE1_BLAST_RADIUS = 1;
    public static final int STAGE2_BLAST_RADIUS = 1;
    public static final int STAGE3_BLAST_RADIUS = 2;

    public static final float STAGE1_EXPLODE_SECONDS = 2.0f;
    public static final float STAGE2_EXPLODE_SECONDS = 2.0f;
    public static final float STAGE3_EXPLODE_SECONDS = 3.3333f;
    public static final float TRANSFORM_SECONDS = 2.0f;
    public static final float EXPLOSION_DAMAGE_DELAY_SECONDS = 0.5f;

    private DoomShroomMuzzles() {
    }

    public static int transformTicks() {
        return secondsToTicks(TRANSFORM_SECONDS);
    }

    public static int explosionDamageDelayTicks() {
        return secondsToTicks(EXPLOSION_DAMAGE_DELAY_SECONDS);
    }

    public static int stage1ToStage2Ticks() {
        return secondsToTicks(STAGE1_TO_STAGE2_SECONDS);
    }

    public static int stage2ToStage3Ticks() {
        return secondsToTicks(STAGE2_TO_STAGE3_SECONDS);
    }

    public static int craterDurationTicks() {
        return secondsToTicks(CRATER_DURATION_SECONDS);
    }

    public static int explodeTicks(int growthStage) {
        return switch (Math.max(0, Math.min(2, growthStage))) {
            case 0 -> secondsToTicks(STAGE1_EXPLODE_SECONDS);
            case 1 -> secondsToTicks(STAGE2_EXPLODE_SECONDS);
            default -> secondsToTicks(STAGE3_EXPLODE_SECONDS);
        };
    }

    public static int stageDamage(int growthStage) {
        return switch (Math.max(0, Math.min(2, growthStage))) {
            case 0 -> STAGE1_DAMAGE;
            case 1 -> STAGE2_DAMAGE;
            default -> STAGE3_DAMAGE;
        };
    }

    public static int stageBlastRadius(int growthStage) {
        return switch (Math.max(0, Math.min(2, growthStage))) {
            case 0 -> STAGE1_BLAST_RADIUS;
            case 1 -> STAGE2_BLAST_RADIUS;
            default -> STAGE3_BLAST_RADIUS;
        };
    }

    private static int secondsToTicks(float seconds) {
        return Math.max(1, Math.round(seconds * GameSession.TICKS_PER_SECOND));
    }
}
