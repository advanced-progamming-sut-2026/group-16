package io.github.finalwave.view.gui.render.sync;

import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.plant.ability.SquashAbility;
import io.github.finalwave.view.gui.render.LawnLayout;
import io.github.finalwave.view.gui.render.clip.PlantClips;
import io.github.finalwave.view.gui.widget.PamActor;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;


public final class SquashJumpTracker {
    private final Map<Plant, SquashAbility.Phase> lastPhase = new IdentityHashMap<>();
    private final Set<Plant> smashing = Collections.newSetFromMap(new IdentityHashMap<>());

    public boolean update(Plant plant,
                          PamActor actor,
                          PlantClips clips,
                          float scale,
                          LawnLayout layout,
                          float tickFraction) {
        if (plant == null || actor == null || layout == null || !plant.isSquash()) {
            return false;
        }
        if (!(plant.getAbility() instanceof SquashAbility ability)) {
            return false;
        }
        SquashAbility.Phase phase = ability.phase();
        if (phase == SquashAbility.Phase.IDLE || phase == SquashAbility.Phase.DONE) {
            boolean wasSmashing = smashing.contains(plant);
            if (wasSmashing) {
                resetToIdle(plant, actor, clips, scale, layout);
            }
            smashing.remove(plant);
            lastPhase.remove(plant);
            return wasSmashing;
        }
        smashing.add(plant);
        SquashAbility.Phase previous = lastPhase.put(plant, phase);
        if (previous != phase) {
            applyClip(actor, clips, scale, ability, phase);
        }
        float progress = phaseProgress(ability, phase, tickFraction);
        float clipSeconds = actor.clipDurationSeconds();
        if (clipSeconds > 0f) {
            actor.setStateTime(progress * clipSeconds);
        }
        var center = new com.badlogic.gdx.math.Vector2();
        SquashArcMotion.position(
                layout,
                ability.segmentFromCol(),
                ability.segmentFromRow(),
                ability.segmentToCol(),
                ability.segmentToRow(),
                phase,
                progress,
                center);
        actor.setPosition(center.x - actor.getWidth() / 2f, center.y - actor.getHeight() / 2f);
        return true;
    }

    public void retain(Iterable<Plant> live) {
        if (live == null) {
            smashing.clear();
            lastPhase.clear();
            return;
        }
        Set<Plant> keep = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Plant plant : live) {
            keep.add(plant);
        }
        smashing.retainAll(keep);
        lastPhase.keySet().retainAll(keep);
    }

    public void clear() {
        smashing.clear();
        lastPhase.clear();
    }

    private static void resetToIdle(Plant plant,
                                    PamActor actor,
                                    PlantClips clips,
                                    float scale,
                                    LawnLayout layout) {
        var idle = clips.squashIdle();
        actor.loadPamSync(idle.path());
        actor.forceClip(idle.path(), idle.clip(), scale, true);
        actor.setPlaybackSpeed(1f);
        var center = layout.cellCenter(plant.getCol(), plant.getRow());
        actor.setPosition(center.x - actor.getWidth() / 2f, center.y - actor.getHeight() / 2f);
    }

    private static float phaseProgress(SquashAbility ability,
                                       SquashAbility.Phase phase,
                                       float tickFraction) {
        int total = SquashAbility.phaseTicksFor(phase, ability.plantFoodChain());
        if (total <= 0) {
            return 1f;
        }
        int remaining = ability.phaseTicksRemaining();
        float fraction = Math.max(0f, Math.min(1f, tickFraction));
        return Math.min(1f, (total - remaining + fraction) / total);
    }

    private static void applyClip(PamActor actor,
                                  PlantClips clips,
                                  float scale,
                                  SquashAbility ability,
                                  SquashAbility.Phase phase) {
        boolean plantFood = ability.plantFoodChain();
        var spec = switch (phase) {
            case JUMP_UP_RIGHT -> clips.squashJumpUpRight();
            case JUMP_DOWN_RIGHT -> plantFood
                    ? clips.squashPlantFoodJumpDownRight()
                    : clips.squashJumpDownRight();
            case TURN -> clips.squashTurn();
            case JUMP_UP_LEFT -> clips.squashJumpUpLeft();
            case JUMP_DOWN_LEFT -> plantFood
                    ? clips.squashPlantFoodJumpDownLeft()
                    : clips.squashJumpDownLeft();
            default -> clips.squashIdle();
        };
        actor.loadPamSync(spec.path());
        actor.forceClip(spec.path(), spec.clip(), scale, false);
        float phaseSeconds = SquashAbility.phaseSeconds(phase, plantFood);
        float clipSeconds = actor.clipDurationSeconds();
        if (phaseSeconds > 0f && clipSeconds > 0f) {
            actor.setPlaybackSpeed(clipSeconds / phaseSeconds);
        } else {
            actor.setPlaybackSpeed(1f);
        }
    }
}
