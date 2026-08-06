package io.github.finalwave.model.game.entity.zombie.behavior;

import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.zombie.Zombie;
import io.github.finalwave.model.game.entity.zombie.ZombieBehavior;

import java.util.ArrayList;
import java.util.List;

public final class RowBurnBehavior implements ZombieBehavior {

    private final int triggerTicks;
    private boolean fired;

    public RowBurnBehavior(int triggerTicks) {
        this.triggerTicks = Math.max(1, triggerTicks);
    }

    @Override
    public void execute(Zombie zombie, GameContext context) {
        if (fired || zombie.getTickAge() < triggerTicks) {
            return;
        }
        if (!zombie.tryBeginAbilityAction()) {
            return;
        }
        fired = true;
        int row = zombie.getRow();
        List<Plant> plants = new ArrayList<>();
        for (int col = 0; col < context.getColCount(); col++) {
            Plant plant = context.getPlantAt(col, row);
            if (plant != null && plant.canBeTargetedByZombie()) {
                plants.add(plant);
            }
        }
        for (Plant plant : plants) {
            plant.takeDamage(plant.getHealth());
            context.onPlantDestroyed(plant);
        }
        zombie.takeDirectDamage(zombie.getHealth());
        context.onZombieKilled(zombie);
    }

    public boolean hasFired() {
        return fired;
    }
}
