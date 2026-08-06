package io.github.finalwave.model.game.entity.zombie.behavior;

import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.zombie.Zombie;
import io.github.finalwave.model.game.entity.zombie.ZombieBehavior;

public final class MobilityTraitBehavior implements ZombieBehavior {

    public enum Trait {DODO_BYPASS, BYPASS_DISABLED, STATIONARY}

    private final Trait trait;

    public MobilityTraitBehavior(Trait trait) {
        this.trait = trait;
    }

    @Override
    public void execute(Zombie zombie, GameContext context) {
        switch (trait) {
            case DODO_BYPASS -> zombie.setDodoBypass(true);
            case BYPASS_DISABLED -> zombie.setBypassDisabledPlants(true);
            case STATIONARY -> zombie.setStationary(true);
        }
    }
}
