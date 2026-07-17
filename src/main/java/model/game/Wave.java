package model.game;

import model.game.entity.zombie.Zombie;

import java.util.ArrayList;
import java.util.List;

public final class Wave {

    private final int number;
    private final int targetCost;
    private final boolean flagWave;
    private final List<Zombie> spawnedZombies = new ArrayList<>();
    private int totalHealthSpawned;
    private boolean started;
    private boolean completed;

    public Wave(int number, int targetCost, boolean flagWave) {
        if (number < 1) {
            throw new IllegalArgumentException("wave number must be >= 1");
        }
        if (targetCost < 1) {
            throw new IllegalArgumentException("targetCost must be >= 1");
        }
        this.number = number;
        this.targetCost = targetCost;
        this.flagWave = flagWave;
    }

    public int getNumber() {
        return number;
    }

    public int getTargetCost() {
        return targetCost;
    }

    public boolean isFlagWave() {
        return flagWave;
    }

    public boolean isStarted() {
        return started;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void markStarted() {
        started = true;
    }

    public void markCompleted() {
        completed = true;
    }

    public void registerSpawn(Zombie zombie) {
        if (zombie == null) {
            return;
        }
        spawnedZombies.add(zombie);
        totalHealthSpawned += totalHealthOf(zombie);
    }

    public int getTotalHealthSpawned() {
        return totalHealthSpawned;
    }

    public int getRemainingHealth() {
        int remaining = 0;
        for (Zombie zombie : spawnedZombies) {
            if (zombie.isAlive()) {
                remaining += totalHealthOf(zombie);
            }
        }
        return remaining;
    }

    public double getDestroyedHealthRatio() {
        if (totalHealthSpawned <= 0) {
            return 1.0;
        }
        return 1.0 - ((double) getRemainingHealth() / totalHealthSpawned);
    }

    public boolean isCleared() {
        for (Zombie zombie : spawnedZombies) {
            if (zombie.isAlive()) {
                return false;
            }
        }
        return started;
    }

    public List<Zombie> getSpawnedZombies() {
        return List.copyOf(spawnedZombies);
    }

    private static int totalHealthOf(Zombie zombie) {
        int health = zombie.getHealth();
        for (var armor : zombie.getArmorLayers()) {
            if (!armor.isDestroyed()) {
                health += armor.getHealth();
            }
        }
        return Math.max(1, health);
    }
}
