package io.github.finalwave.network.match;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
    private String phase;
    private int secondsLeft;
    private List<String> plantLoadout;
    private List<String> zombieRoster;
    private Map<String, Integer> zombieCosts;

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

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    public int getSecondsLeft() {
        return secondsLeft;
    }

    public void setSecondsLeft(int secondsLeft) {
        this.secondsLeft = secondsLeft;
    }

    public List<String> getPlantLoadout() {
        return plantLoadout;
    }

    public void setPlantLoadout(List<String> plantLoadout) {
        this.plantLoadout = plantLoadout == null ? null : new ArrayList<>(plantLoadout);
    }

    public List<String> getZombieRoster() {
        return zombieRoster;
    }

    public void setZombieRoster(List<String> zombieRoster) {
        this.zombieRoster = zombieRoster == null ? null : new ArrayList<>(zombieRoster);
    }

    public Map<String, Integer> getZombieCosts() {
        return zombieCosts;
    }

    public void setZombieCosts(Map<String, Integer> zombieCosts) {
        this.zombieCosts = zombieCosts == null ? null : new LinkedHashMap<>(zombieCosts);
    }
}
