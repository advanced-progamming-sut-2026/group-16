package io.github.finalwave.view.gui.render.sync;

import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.view.gui.render.clip.DoomShroomClips;
import io.github.finalwave.view.gui.render.clip.PlantClips;
import io.github.finalwave.view.gui.widget.PamActor;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;


public final class DoomShroomSequenceTracker {
    private final Set<Plant> pendingSpawn = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Set<Plant> handled = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Set<Plant> detonating = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Map<Plant, Integer> lastStage = new IdentityHashMap<>();
    private final Map<Plant, Boolean> lastAlert = new IdentityHashMap<>();

    public void onSpawn(Plant plant) {
        if (plant == null || !plant.isDoomShroom()) {
            return;
        }
        pendingSpawn.add(plant);
    }

    public boolean update(Plant plant, PamActor actor, PlantClips clips, float scale) {
        if (plant == null || actor == null || !plant.isDoomShroom()) {
            return false;
        }
        if (plant.isDoomShroomDetonating()) {
            if (!detonating.contains(plant)) {
                detonating.add(plant);
                handled.add(plant);
                pendingSpawn.remove(plant);
                var explode = clips.doomShroomExplode(plant.getDoomShroomGrowthStage());
                actor.loadPamSync(explode.path());
                actor.playOnce(explode.path(), explode.clip(), scale, null);
            }
            return true;
        }
        if (plant.isDoomShroomPlantFoodTransforming()) {
            handled.add(plant);
            pendingSpawn.remove(plant);
            int stage = plant.getDoomShroomGrowthStage();
            Integer previousStage = lastStage.get(plant);
            if (previousStage == null || previousStage != stage) {
                int fromStage = previousStage == null ? Math.max(0, stage - 1) : previousStage;
                lastStage.put(plant, stage);
                lastAlert.put(plant, plant.isDoomShroomProximityAlert());
                var transform = clips.doomShroomTransform(fromStage);
                var idle = clips.doomShroomIdle(plant);
                actor.playOnce(transform.path(), transform.clip(), scale,
                        () -> actor.setClip(idle.path(), idle.clip(), scale, true));
            }
            return true;
        }
        if (pendingSpawn.remove(plant)) {
            handled.add(plant);
            lastStage.put(plant, plant.getDoomShroomGrowthStage());
            lastAlert.put(plant, plant.isDoomShroomProximityAlert());
            var spawn = clips.doomShroomStage1Spawn();
            var idle = clips.doomShroomIdle(plant);
            actor.loadPamSync(spawn.path());
            actor.playOnce(spawn.path(), spawn.clip(), scale,
                    () -> actor.setClip(idle.path(), idle.clip(), scale, true));
            return true;
        }
        if (!handled.contains(plant)) {
            return false;
        }
        int stage = plant.getDoomShroomGrowthStage();
        boolean alert = plant.isDoomShroomProximityAlert();
        Integer previousStage = lastStage.get(plant);
        if (previousStage != null && previousStage != stage) {
            lastStage.put(plant, stage);
            lastAlert.put(plant, alert);
            var transform = clips.doomShroomTransform(previousStage);
            var idle = clips.doomShroomIdle(plant);
            actor.playOnce(transform.path(), transform.clip(), scale,
                    () -> actor.setClip(idle.path(), idle.clip(), scale, true));
            return true;
        }
        Boolean previousAlert = lastAlert.get(plant);
        if (previousAlert == null || previousAlert != alert) {
            lastAlert.put(plant, alert);
            lastStage.put(plant, stage);
            var idle = clips.doomShroomIdle(plant);
            actor.setClip(idle.path(), idle.clip(), scale, true);
            return true;
        }
        return true;
    }

    public boolean blocksClipUpdate(Plant plant) {
        return plant != null && (handled.contains(plant) || pendingSpawn.contains(plant));
    }

    public void retain(Iterable<Plant> live) {
        retainOne(pendingSpawn, live);
        retainOne(handled, live);
        retainOne(detonating, live);
        retainMap(lastStage, live);
        retainMap(lastAlert, live);
    }

    public void clear() {
        pendingSpawn.clear();
        handled.clear();
        detonating.clear();
        lastStage.clear();
        lastAlert.clear();
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

    private static <T> void retainMap(Map<Plant, T> tracked, Iterable<Plant> live) {
        if (live == null) {
            tracked.clear();
            return;
        }
        Set<Plant> keep = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Plant plant : live) {
            keep.add(plant);
        }
        tracked.keySet().retainAll(keep);
    }
}
