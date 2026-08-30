package io.github.finalwave.model.game.entity.plant.ability;

import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.projectile.GrapeshotMuzzles;

public final class GrapeshotAbility implements PlantAbility {

    public enum Phase {
        IDLE,
        ATTACK,
        DONE
    }

    private final double radius;
    private Phase phase = Phase.IDLE;
    private int phaseTicksRemaining;

    public GrapeshotAbility(double radius) {
        this.radius = radius;
    }

    public Phase phase() {
        return phase;
    }

    public boolean isActive() {
        return phase != Phase.DONE;
    }

    @Override
    public void onPlanted(Plant plant, GameContext context) {
        phase = Phase.IDLE;
        phaseTicksRemaining = GrapeshotMuzzles.idleTicks();
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
                phaseTicksRemaining = GrapeshotMuzzles.attackSpawnTicks();
            }
            case ATTACK -> detonate(plant, context);
            default -> {
            }
        }
    }

    private void detonate(Plant plant, GameContext context) {
        phase = Phase.DONE;
        int damage = plant.getStats().damage();
        context.explode(plant, damage, radius);
        context.spawnGrapeshotGrapes(plant, GrapeshotMuzzles.GRAPE_COUNT, damage);
        plant.consumeInstantly();
    }
}
