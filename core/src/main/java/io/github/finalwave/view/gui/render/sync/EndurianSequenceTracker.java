package io.github.finalwave.view.gui.render.sync;

import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.view.gui.assets.EntityAnimationCatalog;
import io.github.finalwave.view.gui.render.clip.EndurianClips;
import io.github.finalwave.view.gui.render.clip.PlantClips;
import io.github.finalwave.view.gui.widget.PamActor;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;


public final class EndurianSequenceTracker {
    private final Set<Plant> idling = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Set<Plant> attacking = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Set<Plant> ending = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Set<Plant> plantFooding = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Set<Plant> armored = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Map<Plant, Integer> lastDamageStage = new IdentityHashMap<>();

    public boolean update(Plant plant, PamActor actor, PlantClips clips, float scale) {
        if (plant == null || actor == null || !plant.isEndurian()) {
            return false;
        }
        if (plant.isPlantFooding()) {
            stopIdle(plant);
            attacking.remove(plant);
            ending.remove(plant);
            lastDamageStage.remove(plant);
            if (!plantFooding.contains(plant)) {
                plantFooding.add(plant);
                armored.add(plant);
                playPlantFoodOn(plant, actor, scale);
            }
            applyArmor(plant, actor, false);
            return true;
        }
        plantFooding.remove(plant);
        if (plant.isAttacking()) {
            stopIdle(plant);
            ending.remove(plant);
            if (!attacking.contains(plant)) {
                attacking.add(plant);
                playAttack(actor, scale, plant.endurianDamageStage());
            }
            applyArmor(plant, actor, true);
            return true;
        }
        if (attacking.remove(plant)) {
            playAttackEnd(plant, actor, scale);
            applyArmor(plant, actor, false);
            return true;
        }
        if (ending.contains(plant)) {
            int stage = plant.endurianDamageStage();
            if (actor.isClipFinished() || !EndurianClips.isAttackEndClip(stage, actor.clipName())) {
                ending.remove(plant);
                startIdle(plant, actor, scale);
            }
            applyArmor(plant, actor, false);
            return true;
        }
        applyArmor(plant, actor, false);
        int damageStage = plant.endurianDamageStage();
        if (damageStage == 0) {
            lastDamageStage.remove(plant);
            if (!idling.contains(plant)
                    || !EndurianClips.isIdleClip(actor.clipName())
                    || actor.isClipFinished()) {
                startIdle(plant, actor, scale);
            }
            return true;
        }
        stopIdle(plant);
        Integer previous = lastDamageStage.get(plant);
        if (previous == null
                || previous != damageStage
                || !EndurianClips.isDamageClip(damageStage, actor.clipName())
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
        attacking.retainAll(keep);
        ending.retainAll(keep);
        plantFooding.retainAll(keep);
        armored.retainAll(keep);
        lastDamageStage.keySet().retainAll(keep);
    }

    public void clear() {
        idling.clear();
        attacking.clear();
        ending.clear();
        plantFooding.clear();
        armored.clear();
        lastDamageStage.clear();
    }

    private void startIdle(Plant plant, PamActor actor, float scale) {
        int stage = plant.endurianDamageStage();
        if (stage > 0) {
            lastDamageStage.put(plant, stage);
            loopDamage(actor, scale, stage);
            return;
        }
        idling.add(plant);
        lastDamageStage.remove(plant);
        EntityAnimationCatalog.ClipSpec first = EndurianClips.randomIdle();
        EntityAnimationCatalog.ClipSpec next = EndurianClips.otherIdle(first.clip());
        actor.loadPamSync(first.path());
        actor.forceClip(first.path(), first.clip(), scale, false);
        actor.playThen(first.path(), first.clip(), scale, next.clip(), true, null);
    }

    private void stopIdle(Plant plant) {
        idling.remove(plant);
    }

    private static void playPlantFoodOn(Plant plant, PamActor actor, float scale) {
        var on = EndurianClips.plantFoodOn();
        var idle = EndurianClips.bodyIdle(plant.endurianDamageStage());
        actor.loadPamSync(on.path());
        actor.forceClip(on.path(), on.clip(), scale, false);
        actor.playThen(on.path(), on.clip(), scale, idle.clip(), true, null);
    }

    private static void playAttack(PamActor actor, float scale, int stage) {
        var start = EndurianClips.attackStart(stage);
        var loop = EndurianClips.attackLoop(stage);
        actor.loadPamSync(start.path());
        actor.forceClip(start.path(), start.clip(), scale, false);
        actor.playThen(start.path(), start.clip(), scale, loop.clip(), true, null);
    }

    private void playAttackEnd(Plant plant, PamActor actor, float scale) {
        ending.add(plant);
        var end = EndurianClips.attackEnd(plant.endurianDamageStage());
        actor.loadPamSync(end.path());
        actor.forceClip(end.path(), end.clip(), scale, false);
        actor.playOnce(end.path(), end.clip(), scale, () -> {
            ending.remove(plant);
            if (plant.isAlive() && !plant.isAttacking() && !plant.isPlantFooding()) {
                startIdle(plant, actor, scale);
            }
        });
    }

    private static void loopDamage(PamActor actor, float scale, int stage) {
        var clip = EndurianClips.damage(stage);
        actor.loadPamSync(clip.path());
        actor.forceClip(clip.path(), clip.clip(), scale, true);
        actor.setClip(clip.path(), clip.clip(), scale, true);
    }

    private void applyArmor(Plant plant, PamActor actor, boolean attackingPose) {
        if (plant.hasSmashArmor()) {
            armored.add(plant);
            int stage = Math.max(1, plant.endurianArmorStage());
            actor.setVisibility(EndurianClips.armorVisibility(stage, attackingPose));
            return;
        }
        if (armored.remove(plant)) {
            actor.setVisibility(null);
        }
    }
}
