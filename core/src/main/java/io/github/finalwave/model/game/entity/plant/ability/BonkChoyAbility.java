package io.github.finalwave.model.game.entity.plant.ability;

import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.projectile.BonkChoyMuzzles;
import io.github.finalwave.model.game.entity.zombie.Zombie;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public final class BonkChoyAbility implements PlantAbility {

    public enum PunchStyle {
        RIGHT,
        LEFT,
        BOTH,
        UP_RIGHT,
        UP_LEFT
    }

    private int windupRemaining;
    private PunchStyle pendingStyle = PunchStyle.RIGHT;

    public PunchStyle punchStyle() {
        return pendingStyle;
    }

    @Override
    public boolean tryAction(Plant plant, GameContext context) {
        if (windupRemaining > 0) {
            windupRemaining--;
            if (windupRemaining == 0) {
                executePunch(plant, context);
                plant.setAttacking(false);
                return true;
            }
            return false;
        }
        PunchStyle style = resolvePunchStyle(plant, context);
        if (style == null) {
            return false;
        }
        pendingStyle = style;
        windupRemaining = BonkChoyMuzzles.windupTicks(style);
        plant.setAttacking(true);
        return false;
    }

    @Override
    public int actionWindupTicks() {
        return BonkChoyMuzzles.windupTicks(PunchStyle.RIGHT);
    }

    private void executePunch(Plant plant, GameContext context) {
        int damage = plant.getStats().damage();
        context.dealBonkChoyPunch(plant, pendingStyle, damage);
    }

    static PunchStyle resolvePunchStyle(Plant plant, GameContext context) {
        List<Target> targets = gatherTargets(plant, context);
        if (targets.isEmpty()) {
            return null;
        }
        boolean front = false;
        boolean behind = false;
        for (Target target : targets) {
            if (target.dRow != 0) {
                continue;
            }
            if (target.dCol > 0) {
                front = true;
            } else if (target.dCol < 0) {
                behind = true;
            } else {
                front = true;
            }
        }
        if (front && behind) {
            return PunchStyle.BOTH;
        }
        Target closest = targets.stream()
                .min(Comparator.comparingInt(Target::manhattan))
                .orElse(null);
        if (closest == null) {
            return null;
        }
        return mapRelative(closest.dRow, closest.dCol);
    }

    private static PunchStyle mapRelative(int dRow, int dCol) {
        if (dRow == -1 && dCol > 0) {
            return PunchStyle.UP_RIGHT;
        }
        if (dRow == -1 && dCol <= 0) {
            return PunchStyle.UP_LEFT;
        }
        if (dCol >= 0) {
            return PunchStyle.RIGHT;
        }
        return PunchStyle.LEFT;
    }

    private static List<Target> gatherTargets(Plant plant, GameContext context) {
        List<Target> targets = new ArrayList<>();
        int plantCol = plant.getCol();
        int plantRow = plant.getRow();
        int radius = BonkChoyMuzzles.MELEE_RADIUS;
        for (Zombie zombie : context.getAllZombies()) {
            if (!zombie.isAlive()) {
                continue;
            }
            int zCol = (int) Math.floor(zombie.getX());
            int dRow = zombie.getRow() - plantRow;
            int dCol = zCol - plantCol;
            if (Math.abs(dRow) > radius || Math.abs(dCol) > radius) {
                continue;
            }
            if (dRow == 0 && dCol == 0) {
                dCol = 1;
            }
            targets.add(new Target(dRow, dCol));
        }
        return targets;
    }

    private record Target(int dRow, int dCol) {
        int manhattan() {
            return Math.abs(dRow) + Math.abs(dCol);
        }
    }
}
