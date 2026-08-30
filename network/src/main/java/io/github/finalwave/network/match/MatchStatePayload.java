package io.github.finalwave.network.match;

import java.util.List;

public final class MatchStatePayload {
    private String matchId;
    private long tick;
    private double elapsedSeconds;
    private int sunBalance;
    private int zombieSunBalance;
    private boolean[] brainsEaten;
    private List<SnapshotPlant> plants;
    private List<SnapshotZombie> zombies;
    private List<SnapshotProjectile> projectiles;

    public MatchStatePayload() {
    }

    public String getMatchId() {
        return matchId;
    }

    public void setMatchId(String matchId) {
        this.matchId = matchId;
    }

    public long getTick() {
        return tick;
    }

    public void setTick(long tick) {
        this.tick = tick;
    }

    public double getElapsedSeconds() {
        return elapsedSeconds;
    }

    public void setElapsedSeconds(double elapsedSeconds) {
        this.elapsedSeconds = elapsedSeconds;
    }

    public int getSunBalance() {
        return sunBalance;
    }

    public void setSunBalance(int sunBalance) {
        this.sunBalance = sunBalance;
    }

    public int getZombieSunBalance() {
        return zombieSunBalance;
    }

    public void setZombieSunBalance(int zombieSunBalance) {
        this.zombieSunBalance = zombieSunBalance;
    }

    public boolean[] getBrainsEaten() {
        return brainsEaten;
    }

    public void setBrainsEaten(boolean[] brainsEaten) {
        this.brainsEaten = brainsEaten;
    }

    public List<SnapshotPlant> getPlants() {
        return plants;
    }

    public void setPlants(List<SnapshotPlant> plants) {
        this.plants = plants;
    }

    public List<SnapshotZombie> getZombies() {
        return zombies;
    }

    public void setZombies(List<SnapshotZombie> zombies) {
        this.zombies = zombies;
    }

    public List<SnapshotProjectile> getProjectiles() {
        return projectiles;
    }

    public void setProjectiles(List<SnapshotProjectile> projectiles) {
        this.projectiles = projectiles;
    }
}
