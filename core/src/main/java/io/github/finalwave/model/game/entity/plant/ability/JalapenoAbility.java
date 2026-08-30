package io.github.finalwave.model.game.entity.plant.ability;

import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.projectile.JalapenoMuzzles;

public final class JalapenoAbility implements PlantAbility {

    public enum Phase {
        IDLE,
        ATTACK,
        DONE
    }

    private Phase phase = Phase.IDLE;
    private int phaseTicksRemaining;

    public Phase phase() {
        return phase;
    }

    public boolean isActive() {
        return phase != Phase.DONE;
    }

    @Override
    public void onPlanted(Plant plant, GameContext context) {
        phase = Phase.IDLE;
        phaseTicksRemaining = JalapenoMuzzles.idleTicks();
        plant.setAttacking(false);
    }

    public void tick(Plant plant, GameContext context) {
        if (phase == Phase.DONE) {
            return;
        }
        if (phaseTicksRemaining > 0) {
            phaseTicksRemaining--;
            return;
        }
        advancePhase(plant, context);
    }

    private void advancePhase(Plant plant, GameContext context) {
        switch (phase) {
            case IDLE -> {
                phase = Phase.ATTACK;
                plant.setAttacking(true);
                phaseTicksRemaining = JalapenoMuzzles.attackTicks();
            }
            case ATTACK -> ignite(plant, context);
            default -> {
            }
        }
    }

    private void ignite(Plant plant, GameContext context) {
        phase = Phase.DONE;
        context.startJalapenoRowFire(plant, plant.getStats().damage());
        plant.consumeInstantly();
    }
}
