package io.github.finalwave.view.gui.render.sync;

import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.view.gui.render.clip.PlantClips;
import io.github.finalwave.view.gui.widget.PamActor;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;


public final class PotatoMineRecoverTracker {
    private final Set<Plant> recovering = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Set<Plant> alertInRadius = Collections.newSetFromMap(new IdentityHashMap<>());

    public void tryRecoverAfterIntro(Plant plant,
                                     PamActor actor,
                                     PlantClips clips,
                                     float scale,
                                     GameSession session) {
        if (!canStartRecover(plant, session)) {
            return;
        }
        if (PotatoMineProximity.inRecoverRadius(session, plant)) {
            startRecover(plant, actor, clips, scale, session);
        }
    }

    public boolean updateRecover(Plant plant,
                                 PamActor actor,
                                 PlantClips clips,
                                 float scale,
                                 GameSession session) {
        if (plant == null || actor == null || session == null || !plant.isPotatoMine()) {
            return false;
        }
        if (recovering.contains(plant)) {
            if (!plant.isAlive() || plant.isPlantFooding() || plant.isAttacking()) {
                recovering.remove(plant);
                alertInRadius.remove(plant);
                return false;
            }
            return true;
        }
        if (!canStartRecover(plant, session)) {
            if (plant.isPlantFooding() || plant.isAttacking()) {
                alertInRadius.remove(plant);
            }
            return false;
        }
        if (!PotatoMineProximity.inRecoverRadius(session, plant)) {
            alertInRadius.remove(plant);
            return false;
        }
        if (alertInRadius.contains(plant)) {
            return false;
        }
        startRecover(plant, actor, clips, scale, session);
        return recovering.contains(plant);
    }

    public boolean blocksClipUpdate(Plant plant) {
        return plant != null && recovering.contains(plant);
    }

    public boolean isAlertInRadius(Plant plant) {
        return plant != null && alertInRadius.contains(plant);
    }

    public boolean isRecoverPlaying(Plant plant) {
        return plant != null && recovering.contains(plant);
    }

    public void retain(Iterable<Plant> live) {
        if (live == null) {
            recovering.clear();
            alertInRadius.clear();
            return;
        }
        Set<Plant> keep = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Plant plant : live) {
            keep.add(plant);
        }
        recovering.retainAll(keep);
        alertInRadius.retainAll(keep);
    }

    public void clear() {
        recovering.clear();
        alertInRadius.clear();
    }

    private boolean canStartRecover(Plant plant, GameSession session) {
        return plant != null
                && session != null
                && plant.isAlive()
                && plant.isPotatoMine()
                && !plant.isPlantFoodSpawned()
                && !plant.isAttacking()
                && !plant.isPlantFooding();
    }

    private void startRecover(Plant plant, PamActor actor, PlantClips clips, float scale, GameSession session) {
        if (recovering.contains(plant)) {
            return;
        }
        var recover = clips.potatoMineRecover(plant);
        var alertIdle = clips.potatoMineArmedIdle(plant);
        recovering.add(plant);
        alertInRadius.remove(plant);
        actor.loadPamSync(recover.path());
        actor.playThen(recover.path(), recover.clip(), scale, alertIdle.clip(), true, () -> {
            recovering.remove(plant);
            if (!plant.isAlive() || !plant.isPotatoMine() || plant.isAttacking() || plant.isPlantFooding()) {
                return;
            }
            if (PotatoMineProximity.inRecoverRadius(session, plant)) {
                alertInRadius.add(plant);
            }
        });
    }
}
