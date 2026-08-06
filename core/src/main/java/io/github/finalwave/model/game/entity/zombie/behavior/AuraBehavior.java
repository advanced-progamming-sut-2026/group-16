package io.github.finalwave.model.game.entity.zombie.behavior;

import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.zombie.Zombie;
import io.github.finalwave.model.game.entity.zombie.ZombieBehavior;
import io.github.finalwave.model.game.entity.zombie.ZombieState;
import io.github.finalwave.model.game.entity.zombie.Armor;

import java.util.HashSet;
import java.util.Set;

public final class AuraBehavior implements ZombieBehavior {

    private final AuraType type;
    private final double rangeColumns;
    private final int pulseTicks;
    private int ticksUntilPulse;
    private final Set<String> permanentlyBoostedZombieIds = new HashSet<>();
    public AuraBehavior(AuraType type, double rangeColumns, int pulseTicks) {
        this.type = type;
        this.rangeColumns = rangeColumns;
        this.pulseTicks = Math.max(1, pulseTicks);
        this.ticksUntilPulse = this.pulseTicks;
    }

    @Override
    public void execute(Zombie zombie, GameContext context) {
        if (zombie.getState() == ZombieState.DYING) return;
        if (ticksUntilPulse > 0) {
            ticksUntilPulse--;
            return;
        }
        ticksUntilPulse = pulseTicks;

        for (Zombie other : context.getZombiesInRow(zombie.getRow())) {
            if (other == zombie || other.isDead()) continue;

            double distance = Math.abs(other.getX() - zombie.getX());
            if (distance > rangeColumns) continue;

            applyAura(other);
        }
    }

    private void applyAura(Zombie target) {
        switch (type) {
            case GRANT_ARMOR -> {
                if (!target.hasArmor()) {
                    target.grantArmor(new Armor(
                            "AuraKnightArmor", "knight", 1600, true, true));
                }
            }
            case SPEED_BOOST -> {
                if (permanentlyBoostedZombieIds.add(target.getId())) {
                    target.multiplySpeed(1.3);
                }
            }
        }
    }

    public enum AuraType {GRANT_ARMOR, SPEED_BOOST}
}