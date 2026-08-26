package io.github.finalwave.model.save;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.LawnMower;
import io.github.finalwave.model.game.WaveManager;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.zombie.Zombie;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class MatchSaveSnapshot {
    public static final String KIND_ADVENTURE = "adventure";

    public String kind = KIND_ADVENTURE;
    public String chapterKey;
    public int levelIndex;
    public List<String> loadout = new ArrayList<>();
    public List<String> boosted = new ArrayList<>();
    public int sun;
    public int plantFood;
    public int plantsLost;
    public int currentTick;
    public int currentWaveIndex = -1;
    public boolean wavesStarted;
    public boolean allWavesSpawned;
    public boolean wavesAutoStart = true;
    public List<PlantSnap> plants = new ArrayList<>();
    public List<ZombieSnap> zombies = new ArrayList<>();
    public List<Integer> usedMowerRows = new ArrayList<>();
    public List<String> conveyor = new ArrayList<>();

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class PlantSnap {
        public String name;
        public int level;
        public int col;
        public int row;
        public int health;
        public boolean armed;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class ZombieSnap {
        public String alias;
        public double x;
        public int row;
        public int health;
        public int freezeTicks;
    }

    public static MatchSaveSnapshot capture(String chapterKey,
                                            int levelIndex,
                                            Set<String> loadout,
                                            Set<String> boosted,
                                            GameSession session) {
        MatchSaveSnapshot snap = new MatchSaveSnapshot();
        snap.kind = KIND_ADVENTURE;
        snap.chapterKey = chapterKey;
        snap.levelIndex = levelIndex;
        if (loadout != null) {
            snap.loadout.addAll(loadout);
        }
        if (boosted != null) {
            snap.boosted.addAll(boosted);
        }
        if (session == null) {
            return snap;
        }
        snap.sun = session.getSunBalance();
        snap.plantFood = session.getPlantFoodCount();
        snap.plantsLost = session.getPlantsLost();
        snap.currentTick = session.getCurrentTick();
        snap.wavesAutoStart = session.isWavesAutoStart();
        WaveManager waves = session.getWaveManager();
        if (waves != null) {
            snap.currentWaveIndex = waves.getCurrentWave() == null ? -1 : waves.getCurrentWaveNumber() - 1;
            snap.wavesStarted = waves.areWavesStarted();
            snap.allWavesSpawned = waves.areAllWavesSpawned();
        }
        for (Plant plant : session.getBoard().getAllPlants()) {
            if (plant == null || !plant.isAlive()) {
                continue;
            }
            PlantSnap plantSnap = new PlantSnap();
            plantSnap.name = plant.getName();
            plantSnap.level = plant.getLevel();
            plantSnap.col = plant.getCol();
            plantSnap.row = plant.getRow();
            plantSnap.health = plant.getHealth();
            plantSnap.armed = plant.isArmedTrap();
            snap.plants.add(plantSnap);
        }
        for (Zombie zombie : session.getZombies()) {
            if (zombie == null || !zombie.isAlive() || zombie.isBoss()) {
                continue;
            }
            ZombieSnap zombieSnap = new ZombieSnap();
            zombieSnap.alias = zombie.getType();
            zombieSnap.x = zombie.getX();
            zombieSnap.row = zombie.getRow();
            zombieSnap.health = zombie.getHealth();
            zombieSnap.freezeTicks = zombie.getFreezeTicksRemaining();
            snap.zombies.add(zombieSnap);
        }
        for (LawnMower mower : session.getLawnMowers()) {
            if (mower != null && (mower.isUsed() || mower.isActive())) {
                snap.usedMowerRows.add(mower.getRow());
            }
        }
        if (session.isConveyorBeltActive()) {
            snap.conveyor.addAll(session.getConveyorBeltPlants());
        }
        return snap;
    }
}
