package model.collection;

import java.util.Collection;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class PlayerPlantProgress {

    private static final int MAX_LEVEL = 4;
    private static final String[] STARTER_PLANTS = {"Peashooter", "Sunflower", "Wall-nut"};

    private final Map<String, OwnedPlant> ownedPlants = new LinkedHashMap<>();

    public PlayerPlantProgress() {
        for (String starter : STARTER_PLANTS) {
            ownedPlants.put(starter, new OwnedPlant(starter, 1, true, 0));
        }
    }

    public Map<String, OwnedPlant> getOwnedPlants() {
        Map<String, OwnedPlant> snapshots = new LinkedHashMap<>();
        ownedPlants.forEach((name, plant) -> snapshots.put(name, plant.copy()));
        return Map.copyOf(snapshots);
    }

    public Optional<OwnedPlant> getOwnedPlant(String plantName) {
        return Optional.ofNullable(ownedPlants.get(plantName)).map(OwnedPlant::copy);
    }

    OwnedPlant getOrCreate(String plantName) {
        return ownedPlants.computeIfAbsent(plantName, OwnedPlant::new);
    }

    OwnedPlant getMutablePlant(String plantName) {
        return ownedPlants.get(plantName);
    }

    public void unlock(String plantName) {
        OwnedPlant owned = getOrCreate(plantName);
        owned.setUnlocked(true);
        if (owned.getLevel() < 1) {
            owned.setLevel(1);
        }
    }

    public boolean isOwned(String plantName) {
        OwnedPlant owned = ownedPlants.get(plantName);
        return owned != null && owned.isUnlocked();
    }

    public List<String> getUnlockedPlantNames() {
        return ownedPlants.values().stream()
                .filter(OwnedPlant::isUnlocked)
                .map(OwnedPlant::getPlantName)
                .toList();
    }

    public void addSeedPackets(String plantName, int count) {
        if (count < 0) {
            throw new IllegalArgumentException("count must not be negative");
        }
        getOrCreate(plantName).addSeedPackets(count);
    }

    public int getMaxLevel() {
        return MAX_LEVEL;
    }

    public static PlayerPlantProgress fromOwnedPlants(Collection<OwnedPlant> plants) {
        PlayerPlantProgress progress = new PlayerPlantProgress();
        if (plants != null && !plants.isEmpty()) {
            progress.ownedPlants.clear();
            for (OwnedPlant plant : plants) {
                OwnedPlant copy = plant.copy();
                progress.ownedPlants.put(copy.getPlantName(), copy);
            }
        }
        return progress;
    }
}
