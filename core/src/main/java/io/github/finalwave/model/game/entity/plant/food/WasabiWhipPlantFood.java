package io.github.finalwave.model.game.entity.plant.food;

import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.plant.ability.WasabiWhipAbility;
import io.github.finalwave.model.game.entity.projectile.WasabiWhipMuzzles;


public final class WasabiWhipPlantFood {

    public enum Phase {
        NONE,
        ON,
        LOOP,
        OFF
    }

    private static final WasabiWhipAbility.WhipStyle[] PF_CYCLE = {
            WasabiWhipAbility.WhipStyle.RIGHT,
            WasabiWhipAbility.WhipStyle.LEFT
    };

    private Phase phase = Phase.NONE;
    private int ticksRemaining;
    private int durationRemaining;
    private int pulseCooldown;
    private int damageDealt;
    private int pulseIndex;
    private WasabiWhipAbility.WhipStyle whipStyle = WasabiWhipAbility.WhipStyle.RIGHT;

    public boolean start() {
        if (phase != Phase.NONE) {
            return false;
        }
        phase = Phase.ON;
        ticksRemaining = WasabiWhipMuzzles.plantFoodOnTicks();
        durationRemaining = WasabiWhipMuzzles.plantFoodDurationTicks();
        pulseCooldown = 0;
        damageDealt = 0;
        pulseIndex = 0;
        whipStyle = WasabiWhipAbility.WhipStyle.RIGHT;
        return true;
    }

    public void tick(Plant plant, GameContext context) {
        if (phase == Phase.NONE || plant.isDead()) {
            return;
        }
        durationRemaining--;
        if (durationRemaining <= 0 && phase != Phase.OFF) {
            enterOff();
        }
        if (phase == Phase.ON || phase == Phase.LOOP) {
            tickPulses(plant, context);
        }
        if (ticksRemaining > 0) {
            ticksRemaining--;
            return;
        }
        if (phase == Phase.ON) {
            phase = Phase.LOOP;
            ticksRemaining = WasabiWhipMuzzles.plantFoodLoopTicks();
            return;
        }
        if (phase == Phase.LOOP) {
            if (durationRemaining > WasabiWhipMuzzles.plantFoodOffTicks()) {
                ticksRemaining = WasabiWhipMuzzles.plantFoodLoopTicks();
                return;
            }
            enterOff();
            return;
        }
        phase = Phase.NONE;
        ticksRemaining = 0;
        plant.setAttacking(false);
    }

    public Phase phase() {
        return phase;
    }

    public boolean isActive() {
        return phase != Phase.NONE;
    }

    public WasabiWhipAbility.WhipStyle whipStyle() {
        return whipStyle;
    }

    private void enterOff() {
        phase = Phase.OFF;
        ticksRemaining = WasabiWhipMuzzles.plantFoodOffTicks();
    }

    private void tickPulses(Plant plant, GameContext context) {
        if (damageDealt >= WasabiWhipMuzzles.PLANT_FOOD_TOTAL_DAMAGE) {
            return;
        }
        if (pulseCooldown > 0) {
            pulseCooldown--;
            return;
        }
        int remainingBudget = WasabiWhipMuzzles.PLANT_FOOD_TOTAL_DAMAGE - damageDealt;
        int pulseDamage = Math.min(WasabiWhipMuzzles.plantFoodDamagePerPulse(), remainingBudget);
        double radius = Math.max(1, (int) Math.ceil(Math.sqrt(plant.getDefinition().getPlantFoodValue()) / 2.0));
        context.dealBonkChoyAreaPunch(plant, (int) radius, pulseDamage);
        damageDealt += pulseDamage;
        whipStyle = PF_CYCLE[pulseIndex % PF_CYCLE.length];
        pulseIndex++;
        pulseCooldown = WasabiWhipMuzzles.punchIntervalTicks() - 1;
        plant.setAttacking(true);
    }
}
