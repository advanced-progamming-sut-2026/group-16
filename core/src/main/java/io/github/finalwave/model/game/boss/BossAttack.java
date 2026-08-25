package io.github.finalwave.model.game.boss;

public interface BossAttack {

    void start(BossArena arena);

    boolean tick(BossArena arena);
}
