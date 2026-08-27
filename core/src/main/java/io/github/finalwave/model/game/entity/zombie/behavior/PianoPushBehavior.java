package io.github.finalwave.model.game.entity.zombie.behavior;

import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.zombie.Zombie;
import io.github.finalwave.model.game.entity.zombie.ZombieBehavior;

public final class PianoPushBehavior implements ZombieBehavior {

    @Override
    public void execute(Zombie zombie, GameContext context) {
        context.pushPianoObstacle(zombie);
    }

    @Override
    public void onDeath(Zombie zombie, GameContext context) {
        context.releasePianoObstacle(zombie.getId());
    }
}
