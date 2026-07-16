package model.game.entity.zombie.behavior;

import model.game.entity.*;
import model.game.entity.plant.Plant;
import model.game.entity.zombie.Zombie;
import model.game.entity.zombie.ZombieBehavior;
import model.game.entity.zombie.ZombieState;

public final class TransformBehavior implements ZombieBehavior {

    private final TransformType type;
    private final int cooldownTicks;
    private final boolean enabled;
    private boolean used = false;        // for VAULT_OVER (one-time)
    private int ticksUntilNextSmash = 0; // for SMASH
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
        if (!zombie.tryBeginAbilityAction()) return;

        // Vault: jump over the plant, move 1.5 columns past it
        used = true;
        zombie.moveLeft(1.5);
    }

    private void handleSmash(Zombie zombie, GameContext context) {
        if (ticksUntilNextSmash > 0) {
            ticksUntilNextSmash--;
            return;
        }

        Plant target = context.getPlantInFront(zombie.getX(), zombie.getRow());
        if (target != null && target.canBeTargetedByZombie() && zombie.tryBeginAbilityAction()) {
            zombie.attackPlant(target, target.getHealth()); // instakill
            context.onPlantDestroyed(target);
            ticksUntilNextSmash = cooldownTicks;
        }
    }

    public enum TransformType {VAULT_OVER, SMASH}
}