package io.github.finalwave.view.gui.render.sync;

import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.plant.ability.BonkChoyAbility;
import io.github.finalwave.model.game.entity.plant.food.BonkChoyPlantFood;
import io.github.finalwave.view.gui.render.clip.BonkChoyClips;
import io.github.finalwave.view.gui.render.clip.PlantClips;
import io.github.finalwave.view.gui.widget.PamActor;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;


public final class BonkChoySequenceTracker {
    private final Set<Plant> punching = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Map<Plant, BonkChoyPlantFood.Phase> lastPfPhase = new IdentityHashMap<>();
    private final Map<Plant, BonkChoyAbility.PunchStyle> lastStyle = new IdentityHashMap<>();

    public boolean update(Plant plant, PamActor actor, PlantClips clips, float scale) {
        if (plant == null || actor == null || !plant.isBonkChoy()) {
            return false;
        }
        if (plant.isPlantFooding()) {
            punching.remove(plant);
            lastStyle.remove(plant);
            return updatePlantFood(plant, actor, scale);
        }
        if (!plant.isAttacking()) {
            boolean wasPunching = punching.contains(plant);
            if (wasPunching) {
                var idle = BonkChoyClips.idle();
                actor.setClip(idle.path(), idle.clip(), scale, true);
            }
            punching.remove(plant);
            lastStyle.remove(plant);
            return wasPunching;
        }
        BonkChoyAbility.PunchStyle style = plant.bonkChoyPunchStyle();
        BonkChoyAbility.PunchStyle previous = lastStyle.get(plant);
        if (previous != style || !punching.contains(plant)) {
            lastStyle.put(plant, style);
            punching.add(plant);
            playPunch(actor, scale, style);
        }
        return true;
    }

    public boolean blocksClipUpdate(Plant plant) {
        if (plant == null || !plant.isBonkChoy()) {
            return false;
        }
        return plant.isPlantFooding() || punching.contains(plant);
    }

    public void retain(Iterable<Plant> live) {
        if (live == null) {
            punching.clear();
            lastPfPhase.clear();
            lastStyle.clear();
            return;
        }
        Set<Plant> keep = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Plant plant : live) {
            keep.add(plant);
        }
        punching.retainAll(keep);
        lastPfPhase.keySet().retainAll(keep);
        lastStyle.keySet().retainAll(keep);
    }

    public void clear() {
        punching.clear();
        lastPfPhase.clear();
        lastStyle.clear();
    }

    private boolean updatePlantFood(Plant plant, PamActor actor, float scale) {
        BonkChoyPlantFood.Phase phase = plant.bonkChoyPlantFoodPhase();
        BonkChoyPlantFood.Phase previous = lastPfPhase.getOrDefault(plant, BonkChoyPlantFood.Phase.NONE);
        if (phase != previous) {
            onPlantFoodPhase(plant, actor, scale, phase, previous);
            lastPfPhase.put(plant, phase);
        }
        return phase != BonkChoyPlantFood.Phase.NONE;
    }

    private void onPlantFoodPhase(Plant plant,
                                PamActor actor,
                                float scale,
                                BonkChoyPlantFood.Phase phase,
                                BonkChoyPlantFood.Phase previous) {
        switch (phase) {
            case ON -> {
                var on = BonkChoyClips.plantFoodOn();
                actor.loadPamSync(on.path());
                actor.playThen(on.path(), on.clip(), scale, BonkChoyClips.plantFoodLoop().clip(), true, null);
            }
            case LOOP -> {
                if (previous != BonkChoyPlantFood.Phase.ON) {
                    var loop = BonkChoyClips.plantFoodLoop();
                    actor.setClip(loop.path(), loop.clip(), scale, true);
                }
            }
            case OFF -> {
                var off = BonkChoyClips.plantFoodOff();
                var idle = BonkChoyClips.idle();
                actor.playThen(off.path(), off.clip(), scale, idle.clip(), true, () -> {
                    lastPfPhase.remove(plant);
                    lastStyle.remove(plant);
                });
            }
            default -> {
            }
        }
    }

    private static void playPunch(PamActor actor, float scale, BonkChoyAbility.PunchStyle style) {
        var clip = BonkChoyClips.clipFor(style);
        actor.loadPamSync(clip.path());
        actor.playOnce(clip.path(), clip.clip(), scale, null);
    }
}
