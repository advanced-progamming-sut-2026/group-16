package io.github.finalwave.view.gui.render.sync;

import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.view.gui.render.clip.PhatBeetClips;
import io.github.finalwave.view.gui.render.clip.PlantClips;
import io.github.finalwave.view.gui.widget.PamActor;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;


public final class PhatBeetSequenceTracker {
    private final Set<Plant> plantFooding = Collections.newSetFromMap(new IdentityHashMap<>());

    public boolean update(Plant plant, PamActor actor, PlantClips clips, float scale) {
        if (plant == null || actor == null || !plant.isPhatBeet()) {
            return false;
        }
        if (plant.isPlantFooding()) {
            if (!plantFooding.contains(plant)) {
                plantFooding.add(plant);
                var food = PhatBeetClips.plantFood();
                var idle = PhatBeetClips.idle();
                actor.loadPamSync(food.path());
                actor.playThen(food.path(), food.clip(), scale, idle.clip(), true, null);
            }
            return true;
        }
        return plantFooding.remove(plant);
    }

    public boolean blocksClipUpdate(Plant plant) {
        if (plant == null || !plant.isPhatBeet()) {
            return false;
        }
        return plant.isPlantFooding() || plantFooding.contains(plant);
    }

    public void retain(Iterable<Plant> live) {
        if (live == null) {
            plantFooding.clear();
            return;
        }
        Set<Plant> keep = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Plant plant : live) {
            keep.add(plant);
        }
        plantFooding.retainAll(keep);
    }

    public void clear() {
        plantFooding.clear();
    }
}
