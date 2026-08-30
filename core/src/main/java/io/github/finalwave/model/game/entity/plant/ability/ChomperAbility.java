package io.github.finalwave.model.game.entity.plant.ability;

import io.github.finalwave.model.game.board.tile.Tile;
import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.projectile.ChomperMuzzles;
import io.github.finalwave.model.game.entity.zombie.Zombie;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public final class ChomperAbility implements PlantAbility {

    public enum Phase {
        IDLE,
        BITE,
        BITE_END,
        SWALLOW,
        CHEW,
        CHEW_END,
        PF_ON,
        PF_PULL,
        PF_OFF,
        PF_BURP,
        PF_BURP_END
    }

    private static final String[] INEDIBLE_MARKERS = {
            "Gargantuar", "King", "Arcade", "Piano", "AllStar", "Troglobite", "Zombot"
    };

    private Phase phase = Phase.IDLE;
    private int phaseTicksRemaining;
    private int phaseDurationTicks;
    private final List<String> draggedIds = new ArrayList<>();

    public Phase phase() {
        return phase;
    }

    public boolean isPlantFoodActive() {
        return phase == Phase.PF_ON
                || phase == Phase.PF_PULL
                || phase == Phase.PF_OFF
                || phase == Phase.PF_BURP
                || phase == Phase.PF_BURP_END;
    }

    public boolean isChewing() {
        return phase == Phase.CHEW || phase == Phase.CHEW_END;
    }

    public boolean isBusy() {
        return phase != Phase.IDLE;
    }

    @Override
    public void onPlanted(Plant plant, GameContext context) {
        reset(plant, context);
    }

    public void startPlantFood(Plant plant, GameContext context) {
        releaseDragged(context);
        plant.setAttacking(true);
        enterPhase(Phase.PF_ON, ChomperMuzzles.phaseTicks(Phase.PF_ON));
    }

    public void onZombieContact(Plant plant, Zombie zombie, GameContext context) {
        if (phase != Phase.IDLE || plant.isDead() || zombie == null || !zombie.isAlive()) {
            return;
        }
        tryStartAttack(plant, context);
    }

    public void tick(Plant plant, GameContext context) {
        if (plant.isDead()) {
            releaseDragged(context);
            return;
        }
        if (phase == Phase.IDLE) {
            tryStartAttack(plant, context);
            return;
        }
        if (phase == Phase.PF_PULL) {
            dragTowardMouth(plant, context);
        }
        if (phase == Phase.CHEW_END) {
            growlPush(plant, context);
        }
        if (phase == Phase.PF_BURP) {
            burpPush(plant, context);
        }
        if (phaseTicksRemaining > 0) {
            phaseTicksRemaining--;
            return;
        }
        advancePhase(plant, context);
    }

    public void releaseDragged(GameContext context) {
        if (context != null) {
            for (String id : draggedIds) {
                Zombie zombie = findZombie(context, id);
                if (zombie != null) {
                    zombie.setDragLocked(false);
                }
            }
        }
        draggedIds.clear();
    }

    public static boolean isInedible(Zombie zombie) {
        if (zombie == null) {
            return false;
        }
        String type = zombie.getType();
        if (type == null) {
            return false;
        }
        for (String marker : INEDIBLE_MARKERS) {
            if (type.contains(marker)) {
                return true;
            }
        }
        return false;
    }

    private void tryStartAttack(Plant plant, GameContext context) {
        List<Zombie> edibles = ediblesInRange(plant, context);
        if (!edibles.isEmpty()) {
            startSwallow(plant, context, edibles);
            return;
        }
        if (!inediblesInRange(plant, context).isEmpty() || hasFrontGrave(plant, context)) {
            plant.setAttacking(true);
            enterPhase(Phase.BITE, ChomperMuzzles.phaseTicks(Phase.BITE));
        }
    }

    private void startSwallow(Plant plant, GameContext context, List<Zombie> edibles) {
        plant.setAttacking(true);
        lockTargets(edibles.subList(0, Math.min(ChomperMuzzles.SWALLOW_CAPACITY, edibles.size())));
        enterPhase(Phase.SWALLOW, ChomperMuzzles.phaseTicks(Phase.SWALLOW));
        swallowDragged(plant, context);
    }

    private void advancePhase(Plant plant, GameContext context) {
        switch (phase) {
            case BITE -> {
                applyBite(plant, context);
                enterPhase(Phase.BITE_END, ChomperMuzzles.phaseTicks(Phase.BITE_END));
            }
            case BITE_END -> reset(plant, context);
            case SWALLOW -> {
                swallowDragged(plant, context);
                enterPhase(Phase.CHEW, ChomperMuzzles.chewTicks(plant.getStats().actionInterval()));
            }
            case CHEW -> enterPhase(Phase.CHEW_END, ChomperMuzzles.phaseTicks(Phase.CHEW_END));
            case CHEW_END -> reset(plant, context);
            case PF_ON -> {
                lockTargets(nearestZombies(plant, context, ChomperMuzzles.PLANT_FOOD_PULL_COUNT));
                enterPhase(Phase.PF_PULL, ChomperMuzzles.phaseTicks(Phase.PF_PULL));
            }
            case PF_PULL -> {
                resolvePlantFoodPull(plant, context);
                enterPhase(Phase.PF_OFF, ChomperMuzzles.phaseTicks(Phase.PF_OFF));
            }
            case PF_OFF -> enterPhase(Phase.PF_BURP, ChomperMuzzles.phaseTicks(Phase.PF_BURP));
            case PF_BURP -> enterPhase(Phase.PF_BURP_END, ChomperMuzzles.phaseTicks(Phase.PF_BURP_END));
            case PF_BURP_END -> reset(plant, context);
            default -> reset(plant, context);
        }
    }

    private void enterPhase(Phase next, int ticks) {
        phase = next;
        phaseDurationTicks = Math.max(0, ticks);
        phaseTicksRemaining = phaseDurationTicks;
    }

    private void reset(Plant plant, GameContext context) {
        releaseDragged(context);
        phase = Phase.IDLE;
        phaseTicksRemaining = 0;
        phaseDurationTicks = 0;
        plant.setAttacking(false);
    }

    private void lockTargets(List<Zombie> targets) {
        draggedIds.clear();
        for (Zombie zombie : targets) {
            if (zombie == null || !zombie.isAlive()) {
                continue;
            }
            zombie.setDragLocked(true);
            draggedIds.add(zombie.getId());
        }
    }

    private void dragTowardMouth(Plant plant, GameContext context) {
        double mouth = ChomperMuzzles.mouthX(plant.getCol());
        int remaining = Math.max(1, phaseTicksRemaining);
        boolean plantFood = phase == Phase.PF_PULL;
        for (String id : List.copyOf(draggedIds)) {
            Zombie zombie = findZombie(context, id);
            if (zombie == null || !zombie.isAlive()) {
                draggedIds.remove(id);
                continue;
            }
            double step = (mouth - zombie.getX()) / remaining;
            zombie.setDragStep(step);
            zombie.setPosition(zombie.getX() + step, zombie.getRow());
            if (Math.abs(zombie.getX() - mouth) <= ChomperMuzzles.MOUTH_EAT_RADIUS) {
                resolveAtMouth(zombie, context, plantFood);
                draggedIds.remove(id);
            }
        }
    }

    private void swallowDragged(Plant plant, GameContext context) {
        for (String id : List.copyOf(draggedIds)) {
            Zombie zombie = findZombie(context, id);
            if (zombie == null || !zombie.isAlive()) {
                continue;
            }
            resolveAtMouth(zombie, context, false);
        }
        draggedIds.clear();
    }

    private void resolvePlantFoodPull(Plant plant, GameContext context) {
        for (String id : List.copyOf(draggedIds)) {
            Zombie zombie = findZombie(context, id);
            if (zombie == null || !zombie.isAlive()) {
                continue;
            }
            resolveAtMouth(zombie, context, true);
        }
        draggedIds.clear();
    }

    private void resolveAtMouth(Zombie zombie, GameContext context, boolean plantFood) {
        zombie.setDragLocked(false);
        zombie.setDragStep(0);
        if (plantFood && isInedible(zombie)) {
            zombie.takeDamage(ChomperMuzzles.PLANT_FOOD_INEDIBLE_DAMAGE);
            if (zombie.isDead()) {
                zombie.setSwallowed(true);
                context.onZombieKilled(zombie);
            }
            return;
        }
        zombie.setSwallowed(true);
        zombie.takeDirectDamage(zombie.getMaxHealth());
        if (zombie.isDead()) {
            context.onZombieKilled(zombie);
        }
    }

    private void applyBite(Plant plant, GameContext context) {
        for (Zombie zombie : inediblesInRange(plant, context)) {
            zombie.takeDamage(ChomperMuzzles.BITE_DAMAGE);
            if (zombie.isDead()) {
                context.onZombieKilled(zombie);
            }
        }
        int graveCol = plant.getCol() + 1;
        context.damageGraveAt(graveCol, plant.getRow(), ChomperMuzzles.BITE_DAMAGE);
    }

    private void growlPush(Plant plant, GameContext context) {
        if (phaseDurationTicks <= 0) {
            return;
        }
        double elapsed = (phaseDurationTicks - phaseTicksRemaining)
                / (double) context.getTicksPerSecond();
        if (elapsed < ChomperMuzzles.SPECIAL_END_GROWL_START_SECONDS
                || elapsed > ChomperMuzzles.SPECIAL_END_GROWL_END_SECONDS) {
            return;
        }
        int growlTicks = growlTickCount(context.getTicksPerSecond());
        double step = ChomperMuzzles.GROWL_PUSH_TILES / Math.max(1, growlTicks);
        for (Zombie zombie : context.getZombiesInRow(plant.getRow())) {
            if (!zombie.isAlive() || zombie.getX() < plant.getCol()) {
                continue;
            }
            zombie.moveRight(step);
        }
    }

    private void burpPush(Plant plant, GameContext context) {
        int ticks = Math.max(1, phaseDurationTicks);
        double step = ChomperMuzzles.BURP_PUSH_TILES / ticks;
        for (Zombie zombie : context.getZombiesInRow(plant.getRow())) {
            if (!zombie.isAlive()) {
                continue;
            }
            zombie.moveRight(step);
        }
    }

    private static int growlTickCount(int ticksPerSecond) {
        int start = Math.max(0, (int) Math.round(
                ChomperMuzzles.SPECIAL_END_GROWL_START_SECONDS * ticksPerSecond));
        int end = Math.max(start, (int) Math.round(
                ChomperMuzzles.SPECIAL_END_GROWL_END_SECONDS * ticksPerSecond));
        return Math.max(1, end - start + 1);
    }

    private List<Zombie> ediblesInRange(Plant plant, GameContext context) {
        List<Zombie> found = new ArrayList<>();
        for (Zombie zombie : zombiesInRange(plant, context)) {
            if (!isInedible(zombie)) {
                found.add(zombie);
            }
        }
        found.sort(Comparator.comparingDouble(zombie -> Math.abs(zombie.getX() - plant.getCol())));
        return found;
    }

    private List<Zombie> inediblesInRange(Plant plant, GameContext context) {
        List<Zombie> found = new ArrayList<>();
        for (Zombie zombie : zombiesInRange(plant, context)) {
            if (isInedible(zombie)) {
                found.add(zombie);
            }
        }
        return found;
    }

    private List<Zombie> zombiesInRange(Plant plant, GameContext context) {
        List<Zombie> found = new ArrayList<>();
        int plantCol = plant.getCol();
        int plantRow = plant.getRow();
        int rangeTiles = Math.max(1, (int) Math.floor(Math.max(1.0, plant.getDefinition().getAbilityValue())));
        int frontMin = plantCol + 1;
        int frontMax = plantCol + rangeTiles;
        for (Zombie zombie : context.getZombiesInRow(plantRow)) {
            if (!zombie.isAlive() || zombie.isHypnotized()) {
                continue;
            }
            int zCol = (int) Math.floor(zombie.getX());
            Plant eating = context.getPlantInFront(zombie.getX(), plantRow);
            boolean onFrontTiles = zCol >= frontMin && zCol <= frontMax;
            boolean chewingThis = eating == plant;
            if (onFrontTiles || chewingThis) {
                found.add(zombie);
            }
        }
        return found;
    }

    private List<Zombie> nearestZombies(Plant plant, GameContext context, int count) {
        List<Zombie> found = new ArrayList<>();
        double mouth = ChomperMuzzles.mouthX(plant.getCol());
        for (Zombie zombie : context.getZombiesInRow(plant.getRow())) {
            if (!zombie.isAlive() || zombie.isHypnotized()) {
                continue;
            }
            found.add(zombie);
        }
        found.sort(Comparator.comparingDouble(zombie -> Math.abs(zombie.getX() - mouth)));
        if (found.size() > count) {
            return new ArrayList<>(found.subList(0, count));
        }
        return found;
    }

    private boolean hasFrontGrave(Plant plant, GameContext context) {
        Tile tile = context.getTileAt(plant.getCol() + 1, plant.getRow());
        return tile != null && tile.isGrave();
    }

    private static Zombie findZombie(GameContext context, String id) {
        if (id == null) {
            return null;
        }
        for (Zombie zombie : context.getAllZombies()) {
            if (id.equals(zombie.getId())) {
                return zombie;
            }
        }
        return null;
    }
}
