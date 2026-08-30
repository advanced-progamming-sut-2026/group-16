package io.github.finalwave.view.gui.render.sync;

import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.plant.ability.WasabiWhipAbility;
import io.github.finalwave.model.game.entity.plant.food.WasabiWhipPlantFood;
import io.github.finalwave.view.gui.assets.EntityAnimationCatalog;
import io.github.finalwave.view.gui.render.clip.PlantClips;
import io.github.finalwave.view.gui.render.clip.WasabiWhipClips;
import io.github.finalwave.view.gui.widget.PamActor;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;


public final class WasabiWhipSequenceTracker {
    private final Set<Plant> whipping = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Set<Plant> idling = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Map<Plant, WasabiWhipPlantFood.Phase> lastPfPhase = new IdentityHashMap<>();
    private final Map<Plant, WasabiWhipAbility.WhipStyle> lastStyle = new IdentityHashMap<>();
    private final Map<Plant, String> idleClip = new IdentityHashMap<>();

    public boolean update(Plant plant, PamActor actor, PlantClips clips, float scale) {
        if (plant == null || actor == null || !plant.isWasabiWhip()) {
            return false;
        }
        if (plant.isPlantFooding()) {
            whipping.remove(plant);
            lastStyle.remove(plant);
            stopIdle(plant);
            updatePlantFood(plant, actor, scale);
            return true;
        }
        if (plant.isAttacking()) {
            stopIdle(plant);
            WasabiWhipAbility.WhipStyle style = plant.wasabiWhipStyle();
            WasabiWhipAbility.WhipStyle previous = lastStyle.get(plant);
            if (previous != style || !whipping.contains(plant)) {
                lastStyle.put(plant, style);
                whipping.add(plant);
                playWhip(actor, scale, style);
            }
            return true;
        }
        whipping.remove(plant);
        lastStyle.remove(plant);
        if (!idling.contains(plant)
                || !WasabiWhipClips.isIdleClip(actor.clipName())
                || actor.isClipFinished()) {
            startIdle(plant, actor, scale);
        }
        return true;
    }

    public boolean blocksClipUpdate(Plant plant) {
        return plant != null && plant.isWasabiWhip();
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
        whipping.retainAll(keep);
        idling.retainAll(keep);
        lastPfPhase.keySet().retainAll(keep);
        lastStyle.keySet().retainAll(keep);
        idleClip.keySet().retainAll(keep);
    }

    public void clear() {
        whipping.clear();
        idling.clear();
        lastPfPhase.clear();
        lastStyle.clear();
        idleClip.clear();
    }

    private boolean updatePlantFood(Plant plant, PamActor actor, float scale) {
        WasabiWhipPlantFood.Phase phase = plant.wasabiWhipPlantFoodPhase();
        WasabiWhipPlantFood.Phase previous = lastPfPhase.getOrDefault(plant, WasabiWhipPlantFood.Phase.NONE);
        if (phase != previous) {
            onPlantFoodPhase(plant, actor, scale, phase, previous);
            lastPfPhase.put(plant, phase);
        }
        return phase != WasabiWhipPlantFood.Phase.NONE;
    }

    private void onPlantFoodPhase(Plant plant,
                                PamActor actor,
                                float scale,
                                WasabiWhipPlantFood.Phase phase,
                                WasabiWhipPlantFood.Phase previous) {
        switch (phase) {
            case ON -> {
                var on = WasabiWhipClips.plantFoodOn();
                actor.loadPamSync(on.path());
                actor.playThen(on.path(), on.clip(), scale, WasabiWhipClips.plantFoodLoop().clip(), true, null);
            }
            case LOOP -> {
                if (previous != WasabiWhipPlantFood.Phase.ON) {
                    var loop = WasabiWhipClips.plantFoodLoop();
                    actor.setClip(loop.path(), loop.clip(), scale, true);
                }
            }
            case OFF -> {
                var off = WasabiWhipClips.plantFoodOff();
                var idle = WasabiWhipClips.randomIdle();
                actor.playThen(off.path(), off.clip(), scale, idle.clip(), true, () -> {
                    lastPfPhase.remove(plant);
                    lastStyle.remove(plant);
                    if (plant.isAlive() && !plant.isAttacking() && !plant.isPlantFooding()) {
                        startIdle(plant, actor, scale);
                    }
                });
            }
            default -> {
            }
        }
    }

    private void startIdle(Plant plant, PamActor actor, float scale) {
        idling.add(plant);
        EntityAnimationCatalog.ClipSpec first = WasabiWhipClips.randomIdle();
        EntityAnimationCatalog.ClipSpec next = WasabiWhipClips.otherIdle(first.clip());
        idleClip.put(plant, next.clip());
        actor.loadPamSync(first.path());
        actor.forceClip(first.path(), first.clip(), scale, false);
        actor.playThen(first.path(), first.clip(), scale, next.clip(), true, null);
    }

    private void stopIdle(Plant plant) {
        idling.remove(plant);
        idleClip.remove(plant);
    }

    private static void playWhip(PamActor actor, float scale, WasabiWhipAbility.WhipStyle style) {
        var clip = WasabiWhipClips.clipFor(style);
        actor.loadPamSync(clip.path());
        actor.forceClip(clip.path(), clip.clip(), scale, false);
        actor.playOnce(clip.path(), clip.clip(), scale, null);
    }
}
