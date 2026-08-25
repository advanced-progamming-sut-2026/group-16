package io.github.finalwave.view.gui.widget;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;


public final class HitFlashTracker<M> {
    private final Map<M, Integer> lastHealth = new IdentityHashMap<>();

    public void observe(M model, int health, PamActor actor) {
        observe(model, health, actor, 0f);
    }

    public void observe(M model, int health, PamActor actor, float flashSeconds) {
        if (model == null || actor == null) {
            return;
        }
        Integer previous = lastHealth.put(model, health);
        if (previous != null && health < previous) {
            if (flashSeconds > 0f) {
                actor.flashHit(flashSeconds);
            } else {
                actor.flashHit();
            }
        }
    }

    public void retain(Iterable<M> live) {
        if (live == null) {
            lastHealth.clear();
            return;
        }
        Set<M> keep = Collections.newSetFromMap(new IdentityHashMap<>());
        for (M model : live) {
            keep.add(model);
        }
        lastHealth.keySet().retainAll(keep);
    }

    public void clear() {
        lastHealth.clear();
    }
}
