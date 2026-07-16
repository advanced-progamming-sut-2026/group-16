package model.game.entity.zombie.behavior;

import model.game.entity.GameContext;
import model.game.entity.zombie.Zombie;
import model.game.entity.zombie.ZombieBehavior;

public final class ArmorBehavior implements ZombieBehavior {

    private final double speedBonusWhenArmorLost;
    private boolean armorWasPresent;
    private boolean reacted = false;

    public ArmorBehavior(double speedBonusWhenArmorLost) {
        this.speedBonusWhenArmorLost = speedBonusWhenArmorLost;
    }

    @Override
    public void execute(Zombie zombie, GameContext context) {
        if (reacted) return;

        if (!armorWasPresent && zombie.hasArmor()) {
            armorWasPresent = true;
        }

        if (armorWasPresent && !zombie.hasArmor()) {
            reacted = true;
            zombie.multiplySpeed(speedBonusWhenArmorLost);
        }
    }
}