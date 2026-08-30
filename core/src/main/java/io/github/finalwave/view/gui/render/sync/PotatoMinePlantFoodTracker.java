package io.github.finalwave.view.gui.render.sync;

import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.plant.food.PotatoMinePlantFood;
import io.github.finalwave.view.gui.render.clip.PlantClips;
import io.github.finalwave.view.gui.widget.PamActor;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;


public final class PotatoMinePlantFoodTracker {
    private final Map<Plant, PotatoMinePlantFood.Phase> lastPhase = new IdentityHashMap<>();
    private final Set<Plant> pfExitPlaying = Collections.newSetFromMap(new IdentityHashMap<>());

    public boolean update(Plant plant, PamActor actor, PlantClips clips, float scale) {
        if (plant == null || actor == null || !plant.isPotatoMine()) {
            return false;
        }
        PotatoMinePlantFood.Phase phase = plant.potatoMinePlantFoodPhase();
        PotatoMinePlantFood.Phase previous = lastPhase.getOrDefault(plant, PotatoMinePlantFood.Phase.NONE);
        if (phase != previous) {
            onPhaseEdge(plant, actor, clips, scale, previous, phase);
            lastPhase.put(plant, phase);
        }
        if (!plant.isPlantFooding()) {
            if (needsPfExit(plant, actor)) {
                startPfExit(plant, actor, clips, scale);
            }
            return pfExitPlaying.contains(plant);
        }
        return true;
    }

    public boolean blocksClipUpdate(Plant plant) {
        if (plant == null) {
            return false;
        }
        return plant.isPlantFooding() || pfExitPlaying.contains(plant);
    }

    public boolean tryPfExitSafety(Plant plant, PamActor actor, PlantClips clips, float scale) {
        if (plant == null || actor == null || plant.isPlantFooding() || pfExitPlaying.contains(plant)) {
            return false;
        }
        String clip = actor.clipName();
        if (clip == null || !clip.startsWith("plantfood")) {
            return false;
        }
        startPfExit(plant, actor, clips, scale);
        return pfExitPlaying.contains(plant);
    }

    public void retain(Iterable<Plant> live) {
        if (live == null) {
            lastPhase.clear();
            pfExitPlaying.clear();
            return;
        }
        Set<Plant> keep = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Plant plant : live) {
            keep.add(plant);
        }
        lastPhase.keySet().retainAll(keep);
        pfExitPlaying.retainAll(keep);
    }

    public void clear() {
        lastPhase.clear();
        pfExitPlaying.clear();
    }

    private void onPhaseEdge(Plant plant,
                             PamActor actor,
                             PlantClips clips,
                             float scale,
                             PotatoMinePlantFood.Phase previous,
                             PotatoMinePlantFood.Phase phase) {
        if (phase == PotatoMinePlantFood.Phase.ON) {
            var spec = clips.potatoMinePlantFood(plant);
            actor.playThen(spec.path(), spec.clip(), scale, "plantfood", true, null);
            return;
        }
        if (phase == PotatoMinePlantFood.Phase.LOOP) {
            if (previous == PotatoMinePlantFood.Phase.ON) {
                return;
            }
            var spec = clips.potatoMinePlantFood(plant);
            actor.playThen(spec.path(), spec.clip(), scale, "plantfood", true, null);
            return;
        }
        if (phase == PotatoMinePlantFood.Phase.OFF) {
            startPfExit(plant, actor, clips, scale);
        }
    }

    private boolean needsPfExit(Plant plant, PamActor actor) {
        if (pfExitPlaying.contains(plant)) {
            return false;
        }
        String clip = actor.clipName();
        return clip != null && clip.startsWith("plantfood");
    }

    private void startPfExit(Plant plant, PamActor actor, PlantClips clips, float scale) {
        if (pfExitPlaying.contains(plant)) {
            return;
        }
        pfExitPlaying.add(plant);
        var off = clips.potatoMinePlantFoodOff(plant);
        var rest = plant.isArmedTrap()
                ? clips.potatoMineArmedIdle(plant)
                : clips.potatoMineUnarmedIdle(plant);
        actor.playThen(off.path(), off.clip(), scale, rest.clip(), true, () -> pfExitPlaying.remove(plant));
    }
}
