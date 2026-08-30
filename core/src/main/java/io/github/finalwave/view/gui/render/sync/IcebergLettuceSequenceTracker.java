package io.github.finalwave.view.gui.render.sync;

import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.plant.ability.IcebergLettuceAbility;
import io.github.finalwave.view.gui.render.clip.IcebergLettuceClips;
import io.github.finalwave.view.gui.render.clip.PlantClips;
import io.github.finalwave.view.gui.widget.PamActor;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;


public final class IcebergLettuceSequenceTracker {
    private final Set<Plant> handled = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Map<Plant, IcebergLettuceAbility.Phase> lastPhase = new IdentityHashMap<>();

    public boolean update(Plant plant, PamActor actor, PlantClips clips, float scale) {
        if (plant == null || actor == null || !plant.isIcebergLettuce()) {
            return false;
        }
        if (!(plant.getAbility() instanceof IcebergLettuceAbility ability)) {
            return false;
        }
        IcebergLettuceAbility.Phase phase = ability.phase();
        if (phase == IcebergLettuceAbility.Phase.IDLE) {
            boolean wasActive = handled.contains(plant);
            if (wasActive) {
                var idle = IcebergLettuceClips.idle();
                actor.setClip(idle.path(), idle.clip(), scale, true);
            }
            handled.remove(plant);
            lastPhase.remove(plant);
            return wasActive;
        }
        handled.add(plant);
        IcebergLettuceAbility.Phase previous = lastPhase.get(plant);
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

    private static void playPhase(PamActor actor, float scale, IcebergLettuceAbility.Phase phase) {
        var idle = IcebergLettuceClips.idle();
        switch (phase) {
            case ATTACK -> {
                var clip = IcebergLettuceClips.attack();
                actor.loadPamSync(clip.path());
                actor.playOnce(clip.path(), clip.clip(), scale, null);
            }
            case PLANT_FOOD -> {
                var clip = IcebergLettuceClips.plantFood();
                actor.loadPamSync(clip.path());
                actor.playOnce(clip.path(), clip.clip(), scale,
                        () -> actor.setClip(idle.path(), idle.clip(), scale, true));
            }
            default -> {
            }
        }
    }
}
