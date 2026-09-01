package io.github.finalwave.model.game.entity.zombie.behavior;

import io.github.finalwave.model.game.entity.*;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.plant.PlantSmash;
import io.github.finalwave.model.game.entity.zombie.Zombie;
import io.github.finalwave.model.game.entity.zombie.ZombieBehavior;
import io.github.finalwave.model.game.entity.zombie.ZombieState;

public final class TransformBehavior implements ZombieBehavior {

    private final TransformType type;
    private final int cooldownTicks;
    private final boolean enabled;
    private boolean used = false;
    private int ticksUntilNextSmash = 0;
    public TransformBehavior(TransformType type, int cooldownTicks) {
        this(type, cooldownTicks, true);
    }

    public TransformBehavior(TransformType type, int cooldownTicks, boolean enabled) {
        this.type = type;
        this.cooldownTicks = Math.max(1, cooldownTicks);
        this.enabled = enabled;
    }

    @Override
    public void execute(Zombie zombie, GameContext context) {
        if (!enabled || zombie.getState() == ZombieState.DYING) return;

        switch (type) {
            case VAULT_OVER -> handleVault(zombie, context);
            case SMASH -> handleSmash(zombie, context);
        }
    }

    private void handleVault(Zombie zombie, GameContext context) {
        if (used) return;

        Plant target = context.getPlantInFront(zombie.getX(), zombie.getRow());
        if (target == null || !target.canBeTargetedByZombie()) return;
        if (!zombie.tryBeginAbilityAction()) {
            return;
        }
        used = true;
        zombie.moveLeft(1.5);
    }

    private void handleSmash(Zombie zombie, GameContext context) {
        if ("ZombieGargantuar".equals(zombie.getType())
                && !zombie.isGargantuarImpSpent()
                && (zombie.hasPendingGargantuarImpThrow() || zombie.getHealthRatio() <= 0.5)) {
            return;
        }
        if (ticksUntilNextSmash > 0) {
            ticksUntilNextSmash--;
            return;
        }

        Plant target = context.getPlantInFront(zombie.getX(), zombie.getRow());
        if (target != null && target.canBeTargetedByZombie() && zombie.beginAbility("smash_left", 18)) {
            PlantSmash.apply(zombie, target, context);
            ticksUntilNextSmash = cooldownTicks;
        }
    }

    public enum TransformType {VAULT_OVER, SMASH}
}