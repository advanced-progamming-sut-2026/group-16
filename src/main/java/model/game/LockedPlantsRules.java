package model.game;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class LockedPlantsRules {

    private final LockedPlantsMode mode;
    private final Set<String> lockedPlants;
    private final Set<String> allowedPlants;

    public LockedPlantsRules(LockedPlantsMode mode, Set<String> lockedPlants, Set<String> allowedPlants) {
        this.mode = mode;
        this.lockedPlants = lockedPlants == null ? Set.of() : Set.copyOf(lockedPlants);
        this.allowedPlants = allowedPlants == null ? Set.of() : Set.copyOf(allowedPlants);
    }

    public LockedPlantsMode getMode() {
        return mode;
    }

    public Set<String> getLockedPlants() {
        return lockedPlants;
    }

    public Set<String> getAllowedPlants() {
        return allowedPlants;
    }

    public boolean isLocked(String name) {
        return name != null && lockedPlants.contains(name);
    }

    public boolean isSelectable(String name, boolean owned) {
        if (!owned || name == null) {
            return false;
        }
        if (isLocked(name)) {
            return false;
        }
        if (allowedPlants.isEmpty()) {
            return true;
        }
        return allowedPlants.contains(name);
    }

    public Set<String> selectableFrom(Iterable<String> ownedPlantNames) {
        Set<String> selectable = new HashSet<>();
        if (ownedPlantNames == null) {
            return Set.of();
        }
        for (String name : ownedPlantNames) {
            if (isSelectable(name, true)) {
                selectable.add(name);
            }
        }
        return Collections.unmodifiableSet(selectable);
    }
}
