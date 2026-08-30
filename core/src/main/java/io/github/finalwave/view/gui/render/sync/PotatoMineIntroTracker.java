package io.github.finalwave.view.gui.render.sync;

import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.view.gui.render.clip.PlantClips;
import io.github.finalwave.view.gui.widget.PamActor;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;


public final class PotatoMineIntroTracker {
    private final Set<Plant> pending = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Set<Plant> introStarted = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Set<Plant> introFinished = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Set<Plant> introRecoverPending = Collections.newSetFromMap(new IdentityHashMap<>());

    public void onSpawn(Plant plant) {
        if (plant == null || !plant.isPotatoMine()) {
            return;
        }
        pending.add(plant);
    }

    public boolean startIntroIfNeeded(Plant plant, PamActor actor, PlantClips clips, float scale) {
        if (plant == null || actor == null || !plant.isPotatoMine() || introFinished.contains(plant)) {
            return false;
        }
        if (introStarted.contains(plant)) {
            return true;
        }
        introStarted.add(plant);
        pending.remove(plant);
        if (plant.isPlantFoodSpawned()) {
            var intro = clips.potatoMineCloneIntro(plant);
            var rest = clips.potatoMineArmedIdle(plant);
            actor.playThen(intro.path(), intro.clip(), scale, rest.clip(), true, () -> introFinished.add(plant));
        } else {
            var intro = clips.potatoMineIntro(plant);
            var rest = clips.potatoMineUnarmedIdle(plant);
            actor.playThen(intro.path(), intro.clip(), scale, rest.clip(), true, () -> {
                introFinished.add(plant);
                introRecoverPending.add(plant);
            });
        }
        return true;
    }

    public boolean consumeIntroRecoverPending(Plant plant) {
        return plant != null && introRecoverPending.remove(plant);
    }

    public boolean isIntroPlaying(Plant plant) {
        return plant != null && introStarted.contains(plant) && !introFinished.contains(plant);
    }

    public void retain(Iterable<Plant> live) {
        retainOne(pending, live);
        retainOne(introStarted, live);
        retainOne(introFinished, live);
        retainOne(introRecoverPending, live);
    }

    public void clear() {
        pending.clear();
        introStarted.clear();
        introFinished.clear();
        introRecoverPending.clear();
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
