package io.github.finalwave.model.game;

import io.github.finalwave.model.adventure.ChapterId;
import io.github.finalwave.model.adventure.LevelType;
import io.github.finalwave.model.game.boss.BossArena;
import io.github.finalwave.model.game.boss.BossAttack;
import io.github.finalwave.model.game.boss.BossAttacks;
import io.github.finalwave.model.game.boss.BossCatalog;
import io.github.finalwave.model.game.entity.zombie.Zombie;

import java.util.Collection;
import java.util.List;
import java.util.Random;

public final class BossHandler implements SpecialLevelHandler {

    private final ChapterId chapter;
    private final ConveyBeltHandler conveyor;
    private final Random random;
    private Zombie boss;
    private BossArena arena;
    private BossAttack attack;
    private int introTicks = BossCatalog.INTRO_TICKS;
    private int lastPhase = 1;
    private boolean victoryIssued;

    public BossHandler(ChapterId chapter, Collection<String> availablePlants, Random random) {
        this.chapter = chapter == null ? ChapterId.ANCIENT_EGYPT : chapter;
        this.random = random == null ? new Random() : random;
        this.conveyor = new ConveyBeltHandler(
                BossCatalog.conveyorPlants(this.chapter, availablePlants), this.random);
    }

    public BossHandler() {
        this(ChapterId.ANCIENT_EGYPT, List.of(), new Random());
    }

    public Zombie getBoss() {
        return boss;
    }

    @Override
    public LevelType getLevelType() {
        return LevelType.BOSS;
    }

    @Override
    public void onLevelStart(GameSession session) {
        session.setWavesAutoStart(false);
        session.activateBoss(BossCatalog.MAX_HEALTH);
        conveyor.onLevelStart(session);
        spawnBoss(session);
        introTicks = BossCatalog.INTRO_TICKS;
        attack = null;
        lastPhase = 1;
        victoryIssued = false;
        session.syncBossHud(1, BossCatalog.MAX_HEALTH, BossCatalog.MAX_HEALTH);
    }

    @Override
    public void onTick(GameSession session) {
        conveyor.onTick(session);
        if (session.getMatchResult() != MatchResult.IN_PROGRESS) {
            return;
        }
        if (boss == null || boss.isDead()) {
            issueVictory(session);
            return;
        }
        session.syncBossHud(boss.getBossPhase(), boss.getHealth(), boss.getMaxHealth());
        notifyPhase(session);
        if (introTicks > 0) {
            introTicks--;
            boss.setPresentationClip("intro");
            return;
        }
        if (boss.isStunned()) {
            boss.setPresentationClip("stun");
            attack = null;
            return;
        }
        if (attack == null) {
            attack = new BossAttacks.Idle();
            attack.start(arena);
            return;
        }
        if (attack.tick(arena)) {
            if (attack instanceof BossAttacks.Idle) {
                attack = BossAttacks.randomSpecial(arena);
            } else {
                attack = new BossAttacks.Idle();
            }
            attack.start(arena);
        }
    }

    private void spawnBoss(GameSession session) {
        int row = Math.min(1, Math.max(0, session.getBoard().getRows() - 2));
        double x = Math.max(1.0, session.getBoard().getCols() - 1.0);
        boss = new Zombie.Builder(BossCatalog.alias(chapter))
                .maxHealth(BossCatalog.MAX_HEALTH)
                .speed(0)
                .damage(0)
                .position(x, row)
                .build();
        boss.configureAsBoss(BossCatalog.ROW_SPAN);
        session.addZombie(boss);
        arena = new BossArena(session, boss, random, chapter);
    }

    private void notifyPhase(GameSession session) {
        int phase = boss.getBossPhase();
        if (phase == lastPhase) {
            return;
        }
        lastPhase = phase;
        if (session.getMatchListener() != null) {
            session.getMatchListener().onBossPhaseChanged(phase);
        }
    }

    private void issueVictory(GameSession session) {
        if (victoryIssued || session.getMatchResult() != MatchResult.IN_PROGRESS) {
            return;
        }
        victoryIssued = true;
        session.syncBossHud(3, 0, BossCatalog.MAX_HEALTH);
        if (session.getMatchListener() != null) {
            session.getMatchListener().onBossDefeated();
        }
        session.winMatch();
    }
}
