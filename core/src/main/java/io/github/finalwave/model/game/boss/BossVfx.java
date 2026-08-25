package io.github.finalwave.model.game.boss;

public record BossVfx(Kind kind, int col, int row) {

    public enum Kind {
        LOCK_RETICLE,
        LOCK_RETICLE_ICE,
        MISSILE_FLIGHT,
        ICE_MISSILE_FLIGHT,
        MISSILE_EGYPT,
        MISSILE_ICE,
        FIREBALL_FLIGHT,
        FIREBALL,
        ICE_WIND,
        SHARK,
        VACUUM,
        GLACIER
    }
}
