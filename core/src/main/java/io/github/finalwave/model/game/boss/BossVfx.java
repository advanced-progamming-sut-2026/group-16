package io.github.finalwave.model.game.boss;

public record BossVfx(Kind kind, int col, int row) {

    public enum Kind {
        LOCK_RETICLE,
        MISSILE_FLIGHT,
        MISSILE_EGYPT,
        MISSILE_ICE,
        FIREBALL_FLIGHT,
        FIREBALL,
        SHARK,
        VACUUM,
        GLACIER
    }
}
