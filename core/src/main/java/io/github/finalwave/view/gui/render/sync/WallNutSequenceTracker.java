package io.github.finalwave.view.gui.render.sync;

import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.view.gui.assets.EntityAnimationCatalog;
import io.github.finalwave.view.gui.render.clip.PlantClips;
import io.github.finalwave.view.gui.render.clip.WallNutClips;
import io.github.finalwave.view.gui.widget.PamActor;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;


public final class WallNutSequenceTracker {
    private final Set<Plant> idling = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Set<Plant> plantFooding = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Set<Plant> armored = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Map<Plant, Integer> lastDamageStage = new IdentityHashMap<>();

    public boolean update(Plant plant, PamActor actor, PlantClips clips, float scale) {
        if (plant == null || actor == null || !plant.isWallNut()) {
            return false;
        }
        if (plant.isPlantFooding()) {
            stopIdle(plant);
            lastDamageStage.remove(plant);
            if (!plantFooding.contains(plant)) {
                plantFooding.add(plant);
                armored.add(plant);
                playPlantFoodOn(actor, scale);
            }
            applyArmor(actor, 1);
            return true;
        }
        plantFooding.remove(plant);
        if (plant.hasSmashArmor()) {
            stopIdle(plant);
            lastDamageStage.remove(plant);
            armored.add(plant);
            int stage = Math.max(1, plant.wallNutArmorStage());
            var loop = WallNutClips.plantFoodLoop(stage);
            if (!loop.clip().equals(actor.clipName()) || actor.isClipFinished()) {
                loopPlantFood(actor, scale, stage);
            }
            applyArmor(actor, stage);
            return true;
        }
        if (armored.remove(plant)) {
            actor.setVisibility(null);
        }
        int damageStage = plant.wallNutDamageStage();
        if (damageStage == 0) {
            lastDamageStage.remove(plant);
            if (!idling.contains(plant)
                    || !WallNutClips.isIdleClip(actor.clipName())
                    || actor.isClipFinished()) {
                startIdle(plant, actor, scale);
            }
            return true;
        }
        stopIdle(plant);
        Integer previous = lastDamageStage.get(plant);
        if (previous == null
                || previous != damageStage
                || !WallNutClips.isDamageClip(damageStage, actor.clipName())
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
        plantFooding.retainAll(keep);
        armored.retainAll(keep);
        lastDamageStage.keySet().retainAll(keep);
    }

    public void clear() {
        idling.clear();
        plantFooding.clear();
        armored.clear();
        lastDamageStage.clear();
    }

    private void startIdle(Plant plant, PamActor actor, float scale) {
        idling.add(plant);
        EntityAnimationCatalog.ClipSpec first = WallNutClips.randomIdle();
        EntityAnimationCatalog.ClipSpec next = WallNutClips.otherIdle(first.clip());
        actor.loadPamSync(first.path());
        actor.forceClip(first.path(), first.clip(), scale, false);
        actor.playThen(first.path(), first.clip(), scale, next.clip(), true, null);
    }

    private void stopIdle(Plant plant) {
        idling.remove(plant);
    }

    private static void playPlantFoodOn(PamActor actor, float scale) {
        var on = WallNutClips.plantFoodOn();
        var loop = WallNutClips.plantFoodLoop(1);
        actor.loadPamSync(on.path());
        actor.forceClip(on.path(), on.clip(), scale, false);
        actor.playThen(on.path(), on.clip(), scale, loop.clip(), true, null);
    }

    private static void loopPlantFood(PamActor actor, float scale, int stage) {
        var loop = WallNutClips.plantFoodLoop(stage);
        actor.loadPamSync(loop.path());
        actor.forceClip(loop.path(), loop.clip(), scale, true);
        actor.setClip(loop.path(), loop.clip(), scale, true);
    }

    private static void loopDamage(PamActor actor, float scale, int stage) {
        var clip = WallNutClips.damage(stage);
        actor.loadPamSync(clip.path());
        actor.forceClip(clip.path(), clip.clip(), scale, true);
        actor.setClip(clip.path(), clip.clip(), scale, true);
    }

    private static void applyArmor(PamActor actor, int stage) {
        actor.setVisibility(WallNutClips.armorVisibility(stage));
    }
}
