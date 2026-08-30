package io.github.finalwave.view.gui.render.sync;

import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.plant.food.KiwibeastPlantFood;
import io.github.finalwave.view.gui.assets.EntityAnimationCatalog;
import io.github.finalwave.view.gui.render.clip.KiwibeastClips;
import io.github.finalwave.view.gui.render.clip.PlantClips;
import io.github.finalwave.view.gui.widget.PamActor;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;


public final class KiwibeastSequenceTracker {
    private final Set<Plant> attacking = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Set<Plant> idling = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Set<Plant> growing = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Set<Plant> plantFooding = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Map<Plant, Integer> lastGrowthToken = new IdentityHashMap<>();
    private final Map<Plant, Integer> lastIdleStage = new IdentityHashMap<>();

    public boolean update(Plant plant, PamActor actor, PlantClips clips, float scale) {
        if (plant == null || actor == null || !plant.isKiwibeast()) {
            return false;
        }
        int stage = plant.kiwibeastStage();
        int token = plant.kiwibeastGrowthToken();
        int previousToken = lastGrowthToken.getOrDefault(plant, 0);
        if (token > previousToken) {
            lastGrowthToken.put(plant, token);
            attacking.remove(plant);
            stopIdle(plant);
            plantFooding.remove(plant);
            playGrowth(plant, actor, scale, stage);
            return true;
        }
        if (growing.contains(plant)) {
            if (actor.isClipFinished() || !isGrowthClip(actor.clipName())) {
                growing.remove(plant);
            } else {
                return true;
            }
        }
        if (plant.isPlantFooding()) {
            attacking.remove(plant);
            stopIdle(plant);
            if (!plantFooding.contains(plant)) {
                plantFooding.add(plant);
                playPlantFood(plant, actor, scale, stage);
            }
            return true;
        }
        if (plantFooding.remove(plant)) {
            startIdle(plant, actor, scale, stage);
            return true;
        }
        if (plant.isAttacking()) {
            stopIdle(plant);
            if (!attacking.contains(plant)) {
                attacking.add(plant);
                playAttack(actor, scale, stage);
            }
            return true;
        }
        attacking.remove(plant);
        Integer idleStage = lastIdleStage.get(plant);
        if (!idling.contains(plant)
                || idleStage == null
                || idleStage != stage
                || !KiwibeastClips.isIdleClip(stage, actor.clipName())
                || actor.isClipFinished()) {
            startIdle(plant, actor, scale, stage);
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
        attacking.retainAll(keep);
        idling.retainAll(keep);
        growing.retainAll(keep);
        plantFooding.retainAll(keep);
        lastGrowthToken.keySet().retainAll(keep);
        lastIdleStage.keySet().retainAll(keep);
    }

    public void clear() {
        attacking.clear();
        idling.clear();
        growing.clear();
        plantFooding.clear();
        lastGrowthToken.clear();
        lastIdleStage.clear();
    }

    private void startIdle(Plant plant, PamActor actor, float scale, int stage) {
        idling.add(plant);
        lastIdleStage.put(plant, stage);
        EntityAnimationCatalog.ClipSpec first = KiwibeastClips.randomIdle(stage);
        EntityAnimationCatalog.ClipSpec next = KiwibeastClips.otherIdle(stage, first.clip());
        actor.loadPamSync(first.path());
        actor.forceClip(first.path(), first.clip(), scale, false);
        actor.playThen(first.path(), first.clip(), scale, next.clip(), true, null);
    }

    private void stopIdle(Plant plant) {
        idling.remove(plant);
        lastIdleStage.remove(plant);
    }

    private void playAttack(PamActor actor, float scale, int stage) {
        var clip = KiwibeastClips.attack(stage);
        actor.loadPamSync(clip.path());
        actor.forceClip(clip.path(), clip.clip(), scale, false);
        actor.playOnce(clip.path(), clip.clip(), scale, null);
    }

    private void playGrowth(Plant plant, PamActor actor, float scale, int stage) {
        growing.add(plant);
        int fromStage = Math.max(1, stage - 1);
        var clip = KiwibeastClips.growth(fromStage);
        var idle = KiwibeastClips.randomIdle(stage);
        actor.loadPamSync(clip.path());
        actor.forceClip(clip.path(), clip.clip(), scale, false);
        actor.playThen(clip.path(), clip.clip(), scale, idle.clip(), true, () -> {
            growing.remove(plant);
            if (plant.isAlive() && !plant.isAttacking() && !plant.isPlantFooding()) {
                startIdle(plant, actor, scale, plant.kiwibeastStage());
            }
        });
    }

    private void playPlantFood(Plant plant, PamActor actor, float scale, int stage) {
        var food = KiwibeastClips.plantFood();
        var idle = KiwibeastClips.randomIdle(Math.max(3, stage));
        actor.loadPamSync(food.path());
        actor.forceClip(food.path(), food.clip(), scale, false);
        actor.playThen(food.path(), food.clip(), scale, idle.clip(), true, () -> {
            plantFooding.remove(plant);
            if (plant.isAlive() && !plant.isAttacking() && plant.kiwibeastPlantFoodPhase()
                    == KiwibeastPlantFood.Phase.NONE) {
                startIdle(plant, actor, scale, plant.kiwibeastStage());
            }
        });
    }

    private static boolean isGrowthClip(String clip) {
        return "growth_stage1".equals(clip)
                || "growth_stage1_2".equals(clip)
                || "growth_stage2".equals(clip);
    }
}
