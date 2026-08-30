package io.github.finalwave.view.gui.render.sync;

import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.view.gui.render.clip.PlantClips;
import io.github.finalwave.view.gui.widget.PamActor;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;


public final class CherryBombSequenceTracker {
    private final Set<Plant> pending = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Set<Plant> started = Collections.newSetFromMap(new IdentityHashMap<>());

    public void onSpawn(Plant plant) {
        if (plant == null || !plant.isCherryBomb()) {
            return;
        }
        pending.add(plant);
    }

    public boolean startSequenceIfNeeded(Plant plant, PamActor actor, PlantClips clips, float scale) {
        if (plant == null || actor == null || !plant.isCherryBomb()) {
            return false;
        }
        if (started.contains(plant)) {
            return true;
        }
        if (!pending.remove(plant)) {
            return false;
        }
        started.add(plant);
        var idle = clips.cherryBombIdle();
        var attack = clips.cherryBombAttack();
        actor.loadPamSync(idle.path());
        actor.playThen(idle.path(), idle.clip(), scale, attack.clip(), false, null);
        return true;
    }

    public boolean blocksClipUpdate(Plant plant) {
        return plant != null && started.contains(plant);
    }

    public void retain(Iterable<Plant> live) {
        retainOne(pending, live);
        retainOne(started, live);
    }

    public void clear() {
        pending.clear();
        started.clear();
    }

    private static void retainOne(Set<Plant> tracked, Iterable<Plant> live) {
        if (live == null) {
            tracked.clear();
            return;
        }
        Set<Plant> keep = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Plant plant : live) {
            keep.add(plant);
        }
        tracked.retainAll(keep);
    }
}
