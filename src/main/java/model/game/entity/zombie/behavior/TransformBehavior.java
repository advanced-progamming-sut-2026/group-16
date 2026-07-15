package model.game.entity.zombie.behavior;

import model.game.entity.*;
import model.game.entity.plant.Plant;
import model.game.entity.zombie.Zombie;
import model.game.entity.zombie.ZombieBehavior;
import model.game.entity.zombie.ZombieState;

public final class TransformBehavior implements ZombieBehavior {

    private final TransformType type;
    private final int cooldownTicks;
    private boolean used = false;        // for VAULT_OVER (one-time)
    private int ticksUntilNextSmash = 0; // for SMASH
    public TransformBehavior(TransformType type, int cooldownTicks) {
        this.type = type;
        this.cooldownTicks = Math.max(1, cooldownTicks);
    }

    @Override
    public void execute(Zombie zombie, GameContext context) {
        if (zombie.getState() == ZombieState.DYING) return;

        switch (type) {
            case VAULT_OVER -> handleVault(zombie, context);
            case SMASH -> handleSmash(zombie, context);
        }
    }

    private void handleVault(Zombie zombie, GameContext context) {
        if (used) return;

        Plant target = context.getPlantInFront(zombie.getX(), zombie.getRow());
        if (target == null || !target.isAlive()) return;

        // Vault: jump over the plant, move 1.5 columns past it
        used = true;
        zombie.setState(ZombieState.ABILITY);
        zombie.moveLeft(1.5);
    }

    private void handleSmash(Zombie zombie, GameContext context) {
        if (ticksUntilNextSmash > 0) {
            ticksUntilNextSmash--;
            return;
        }

        Plant target = context.getPlantInFront(zombie.getX(), zombie.getRow());
        if (target != null && target.isAlive()) {
            zombie.setState(ZombieState.ABILITY);
            zombie.attackPlant(target, target.getHealth()); // instakill
            context.onPlantDestroyed(target);
            ticksUntilNextSmash = cooldownTicks;
        }
    }

    public enum TransformType {VAULT_OVER, SMASH}
}