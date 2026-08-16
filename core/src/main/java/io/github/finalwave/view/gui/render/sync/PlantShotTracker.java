package io.github.finalwave.view.gui.render.sync;

import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.projectile.Projectile;

import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Set;


public final class PlantShotTracker {
    private final Set<String> seenIds = new HashSet<>();
    private final Set<Plant> fired = Collections.newSetFromMap(new IdentityHashMap<>());

    public void observe(Iterable<Projectile> projectiles) {
        if (projectiles == null) {
            return;
        }
        for (Projectile projectile : projectiles) {
            if (projectile == null || projectile.getId() == null) {
                continue;
            }
            if (!seenIds.add(projectile.getId())) {
                continue;
            }
            Plant source = projectile.getSource();
            if (source != null) {
                fired.add(source);
            }
        }
    }

    public boolean consume(Plant plant) {
        return plant != null && fired.remove(plant);
    }

    public void retain(Iterable<Plant> live, Iterable<Projectile> projectiles) {
        Set<String> liveIds = new HashSet<>();
        if (projectiles != null) {
            for (Projectile projectile : projectiles) {
                if (projectile != null && projectile.getId() != null) {
                    liveIds.add(projectile.getId());
                }
            }
        }
        seenIds.retainAll(liveIds);
        if (live == null) {
            fired.clear();
            return;
        }
        Set<Plant> keep = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Plant plant : live) {
            keep.add(plant);
        }
        fired.retainAll(keep);
    }

    public void clear() {
        seenIds.clear();
        fired.clear();
    }
}
