package io.github.finalwave.model.game.entity.zombie.behavior;

import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.zombie.Armor;
import io.github.finalwave.model.game.entity.zombie.Zombie;
import io.github.finalwave.model.game.entity.zombie.ZombieBehavior;

import java.util.Set;

public final class KnightingBehavior implements ZombieBehavior {

    private final int cooldownTicks;
    private final double horizontalRange;
    private final int verticalRange;
    private final Set<String> validTargetKeys;
    private int cooldown;
    private boolean parked;

    public KnightingBehavior(int cooldownTicks, double horizontalRange, int verticalRange) {
        this(cooldownTicks, horizontalRange, verticalRange,
                Set.of("default", "armor1", "armor2", "armor3", "armor4"));
    }

    public KnightingBehavior(int cooldownTicks, double horizontalRange, int verticalRange,
                             Set<String> validTargetKeys) {
        this.cooldownTicks = Math.max(1, cooldownTicks);
        this.horizontalRange = horizontalRange;
        this.verticalRange = verticalRange;
        this.validTargetKeys = validTargetKeys == null ? Set.of() : Set.copyOf(validTargetKeys);
    }

    @Override
    public void execute(Zombie zombie, GameContext context) {
        zombie.setStationary(true);
        double parkedX = Math.max(0, context.getColCount() - 1);
        if (!parked || Math.abs(zombie.getX() - parkedX) > 0.05) {
            zombie.setPosition(parkedX, zombie.getRow());
            parked = true;
        }
        if (cooldown-- > 0) {
            return;
        }
        for (Zombie target : context.getAllZombies()) {
            if (validTarget(zombie, target) && zombie.beginAbility("special", 12)) {
                target.grantArmor(new Armor("KingShoulderArmor", "shoulder", 1600, false, false));
                target.grantArmor(new Armor("KingKnightHelm", "helm", 1600, true, true));
                break;
            }
        }
        cooldown = cooldownTicks;
    }

    private boolean validTarget(Zombie king, Zombie target) {
        return target != king && target.isAlive() && !target.hasArmorAlias("KingKnightHelm")
                && target.isValidKnightTarget(validTargetKeys)
                && Math.abs(target.getX() - king.getX()) <= horizontalRange
                && Math.abs(target.getRow() - king.getRow()) <= verticalRange;
    }
}
