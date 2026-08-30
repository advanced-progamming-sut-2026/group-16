package io.github.finalwave.model.game.entity.plant.food;

import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.plant.ability.BonkChoyAbility;
import io.github.finalwave.model.game.entity.projectile.BonkChoyMuzzles;


public final class BonkChoyPlantFood {

    public enum Phase {
        NONE,
        ON,
        LOOP,
        OFF
    }

    private static final BonkChoyAbility.PunchStyle[] PF_CYCLE = {
            BonkChoyAbility.PunchStyle.RIGHT,
            BonkChoyAbility.PunchStyle.LEFT,
            BonkChoyAbility.PunchStyle.UP_RIGHT,
            BonkChoyAbility.PunchStyle.UP_LEFT,
            BonkChoyAbility.PunchStyle.BOTH
    };

    private Phase phase = Phase.NONE;
    private int ticksRemaining;
    private int durationRemaining;
    private int pulseCooldown;
    private int damageDealt;
    private int pulseIndex;
    private BonkChoyAbility.PunchStyle punchStyle = BonkChoyAbility.PunchStyle.RIGHT;

    public boolean start() {
        if (phase != Phase.NONE) {
            return false;
        }
        phase = Phase.ON;
        ticksRemaining = BonkChoyMuzzles.plantFoodOnTicks();
        durationRemaining = BonkChoyMuzzles.plantFoodDurationTicks();
        pulseCooldown = 0;
        damageDealt = 0;
        pulseIndex = 0;
        punchStyle = BonkChoyAbility.PunchStyle.RIGHT;
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
            ticksRemaining = BonkChoyMuzzles.plantFoodLoopTicks();
            return;
        }
        if (phase == Phase.LOOP) {
            if (durationRemaining > BonkChoyMuzzles.plantFoodOffTicks()) {
                ticksRemaining = BonkChoyMuzzles.plantFoodLoopTicks();
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

    public BonkChoyAbility.PunchStyle punchStyle() {
        return punchStyle;
    }

    private void enterOff() {
        phase = Phase.OFF;
        ticksRemaining = BonkChoyMuzzles.plantFoodOffTicks();
    }

    private void tickPulses(Plant plant, GameContext context) {
        if (damageDealt >= BonkChoyMuzzles.PLANT_FOOD_TOTAL_DAMAGE) {
            return;
        }
        if (pulseCooldown > 0) {
            pulseCooldown--;
            return;
        }
        int remainingBudget = BonkChoyMuzzles.PLANT_FOOD_TOTAL_DAMAGE - damageDealt;
        int pulseDamage = Math.min(BonkChoyMuzzles.plantFoodDamagePerPulse(), remainingBudget);
        double radius = Math.max(1, (int) Math.ceil(Math.sqrt(plant.getDefinition().getPlantFoodValue()) / 2.0));
        context.dealBonkChoyAreaPunch(plant, (int) radius, pulseDamage);
        damageDealt += pulseDamage;
        punchStyle = PF_CYCLE[pulseIndex % PF_CYCLE.length];
        pulseIndex++;
        pulseCooldown = BonkChoyMuzzles.punchIntervalTicks() - 1;
        plant.setAttacking(true);
    }
}
