package io.github.finalwave.view.gui.render.sync;

import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.plant.ability.ChomperAbility;
import io.github.finalwave.view.gui.assets.EntityAnimationCatalog;
import io.github.finalwave.view.gui.render.clip.ChomperClips;
import io.github.finalwave.view.gui.render.clip.PlantClips;
import io.github.finalwave.view.gui.widget.PamActor;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;


public final class ChomperSequenceTracker {
    private final Set<Plant> handled = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Map<Plant, ChomperAbility.Phase> lastPhase = new IdentityHashMap<>();

    public boolean update(Plant plant, PamActor actor, PlantClips clips, float scale) {
        if (plant == null || actor == null || !plant.isChomper()) {
            return false;
        }
        if (!(plant.getAbility() instanceof ChomperAbility ability)) {
            return false;
        }
        ChomperAbility.Phase phase = ability.phase();
        if (phase == ChomperAbility.Phase.IDLE) {
            boolean wasActive = handled.contains(plant);
            if (wasActive) {
                playRandomIdle(plant, actor, scale);
            }
            handled.remove(plant);
            lastPhase.remove(plant);
            return wasActive;
        }
        handled.add(plant);
        ChomperAbility.Phase previous = lastPhase.get(plant);
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

    private static void playPhase(PamActor actor, float scale, ChomperAbility.Phase phase) {
        switch (phase) {
            case BITE -> playForced(actor, scale, ChomperClips.bite(), false);
            case BITE_END -> playForced(actor, scale, ChomperClips.biteEnd(), false);
            case SWALLOW -> playForced(actor, scale, ChomperClips.swallow(), false);
            case CHEW -> playForced(actor, scale, ChomperClips.chew(), true);
            case CHEW_END -> playForced(actor, scale, ChomperClips.chewEnd(), false);
            case PF_ON -> playForced(actor, scale, ChomperClips.plantFoodOn(), false);
            case PF_PULL -> playForced(actor, scale, ChomperClips.plantFood(), false);
            case PF_OFF -> playForced(actor, scale, ChomperClips.plantFoodOff(), false);
            case PF_BURP -> playForced(actor, scale, ChomperClips.plantFoodBurp(), false);
            case PF_BURP_END -> playForced(actor, scale, ChomperClips.plantFoodBurpEnd(), false);
            default -> {
            }
        }
    }

    private static void playForced(PamActor actor,
                                   float scale,
                                   EntityAnimationCatalog.ClipSpec spec,
                                   boolean loop) {
        actor.loadPamSync(spec.path());
        actor.forceClip(spec.path(), spec.clip(), scale, loop);
    }

    private static void playRandomIdle(Plant plant, PamActor actor, float scale) {
        var idle = ChomperClips.randomIdle();
        actor.loadPamSync(idle.path());
        actor.playOnce(idle.path(), idle.clip(), scale, () -> {
            if (plant.isAlive() && plant.chomperPhase() == ChomperAbility.Phase.IDLE) {
                playRandomIdle(plant, actor, scale);
            }
        });
    }
}
