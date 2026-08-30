package io.github.finalwave.view.gui.render.sync;

import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.plant.ability.TangleKelpAbility;
import io.github.finalwave.view.gui.render.clip.PlantClips;
import io.github.finalwave.view.gui.render.clip.TangleKelpClips;
import io.github.finalwave.view.gui.widget.PamActor;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;


public final class TangleKelpSequenceTracker {
    private final Set<Plant> handled = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Map<Plant, TangleKelpAbility.Phase> lastPhase = new IdentityHashMap<>();

    public boolean update(Plant plant, PamActor actor, PlantClips clips, float scale) {
        if (plant == null || actor == null || !plant.isTangleKelp()) {
            return false;
        }
        if (!(plant.getAbility() instanceof TangleKelpAbility ability)) {
            return false;
        }
        TangleKelpAbility.Phase phase = ability.phase();
        if (phase == TangleKelpAbility.Phase.IDLE) {
            boolean wasActive = handled.contains(plant);
            if (wasActive) {
                var idle = TangleKelpClips.idle();
                actor.setClip(idle.path(), idle.clip(), scale, true);
            }
            handled.remove(plant);
            lastPhase.remove(plant);
            return wasActive;
        }
        handled.add(plant);
        TangleKelpAbility.Phase previous = lastPhase.get(plant);
        if (previous != phase) {
            lastPhase.put(plant, phase);
            playPhase(actor, scale, phase);
        }
        return true;
    }

    public boolean blocksClipUpdate(Plant plant) {
        return plant != null && handled.contains(plant);
    }

    public void retain(Iterable<Plant> live) {
        if (live == null) {
            handled.clear();
            lastPhase.clear();
            return;
        }
        Set<Plant> keep = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Plant plant : live) {
            keep.add(plant);
        }
        handled.retainAll(keep);
        lastPhase.keySet().retainAll(keep);
    }

    public void clear() {
        handled.clear();
        lastPhase.clear();
    }

    private static void playPhase(PamActor actor, float scale, TangleKelpAbility.Phase phase) {
        var idle = TangleKelpClips.idle();
        switch (phase) {
            case SUBMERGE -> {
                var clip = TangleKelpClips.attackSubmerge();
                actor.loadPamSync(clip.path());
                actor.playOnce(clip.path(), clip.clip(), scale, null);
            }
            case ATTACK -> {
                var clip = TangleKelpClips.attack();
                actor.playOnce(clip.path(), clip.clip(), scale, null);
            }
            case EMERGE -> {
                var clip = TangleKelpClips.attackEmerge();
                actor.playOnce(clip.path(), clip.clip(), scale,
                        () -> actor.setClip(idle.path(), idle.clip(), scale, true));
            }
            case PLANT_FOOD_ON -> {
                var clip = TangleKelpClips.plantFoodOn();
                actor.loadPamSync(clip.path());
                actor.playOnce(clip.path(), clip.clip(), scale, null);
            }
            case PLANT_FOOD -> {
                var clip = TangleKelpClips.plantFood();
                actor.playOnce(clip.path(), clip.clip(), scale, null);
            }
            case PLANT_FOOD_OFF -> {
                var clip = TangleKelpClips.plantFoodOff();
                actor.playOnce(clip.path(), clip.clip(), scale,
                        () -> actor.setClip(idle.path(), idle.clip(), scale, true));
            }
            default -> {
            }
        }
    }
}
