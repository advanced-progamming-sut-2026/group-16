package model.game.entity.zombie.behavior;

import model.game.entity.GameContext;
import model.game.entity.zombie.Zombie;
import model.game.entity.zombie.ZombieBehavior;
import model.game.entity.zombie.ZombieState;

public final class AuraBehavior implements ZombieBehavior {

    private final AuraType type;
    private final double rangeColumns;
    private final int pulseTicks;
    private int ticksUntilPulse;
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
                    target.getArmorLayers(); // immutable list; actual armor
                    // addition would need builder/factory support.
                    // TODO: Add armor via factory when aura grants armor.
                }
            }
            case SPEED_BOOST -> {
                target.setCurrentSpeed(target.getBaseSpeed() * 1.3);
            }
        }
    }

    public enum AuraType {GRANT_ARMOR, SPEED_BOOST}
}