package io.github.finalwave.view.gui.render.sync;

import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.view.gui.render.clip.PlantClips;
import io.github.finalwave.view.gui.render.clip.TallNutClips;
import io.github.finalwave.view.gui.widget.PamActor;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;


public final class TallNutSequenceTracker {
    private final Set<Plant> idling = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Map<Plant, Integer> lastDamageStage = new IdentityHashMap<>();

    public boolean update(Plant plant, PamActor actor, PlantClips clips, float scale) {
        if (plant == null || actor == null || !plant.isTallNut()) {
            return false;
        }
        int damageStage = plant.tallNutDamageStage();
        if (damageStage == 0) {
            lastDamageStage.remove(plant);
            if (!idling.contains(plant)
                    || !TallNutClips.isIdleClip(actor.clipName())
                    || actor.isClipFinished()) {
                startIdle(plant, actor, scale);
            }
            return true;
        }
        idling.remove(plant);
        Integer previous = lastDamageStage.get(plant);
        if (previous == null
                || previous != damageStage
                || !TallNutClips.isDamageClip(damageStage, actor.clipName())
                || actor.isClipFinished()) {
            lastDamageStage.put(plant, damageStage);
            loopDamage(actor, scale, damageStage);
        }
        return true;
    }

    public void retain(Iterable<Plant> live) {
        if (live == null) {
            clear();
            return;
        }
        Set<Plant> keep = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Plant plant : live) {
            keep.add(plant);
        }
        idling.retainAll(keep);
        lastDamageStage.keySet().retainAll(keep);
    }

    public void clear() {
        idling.clear();
        lastDamageStage.clear();
    }

    private void startIdle(Plant plant, PamActor actor, float scale) {
        idling.add(plant);
        var clip = TallNutClips.idle();
        actor.loadPamSync(clip.path());
        actor.forceClip(clip.path(), clip.clip(), scale, true);
    }

    private static void loopDamage(PamActor actor, float scale, int stage) {
        var clip = TallNutClips.damage(stage);
        actor.loadPamSync(clip.path());
        actor.forceClip(clip.path(), clip.clip(), scale, true);
        actor.setClip(clip.path(), clip.clip(), scale, true);
    }
}
