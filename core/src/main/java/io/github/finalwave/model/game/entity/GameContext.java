package io.github.finalwave.model.game.entity;

import io.github.finalwave.model.game.board.tile.Tile;
import io.github.finalwave.model.game.GooPuddle;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.plant.PlantCategory;
import io.github.finalwave.model.game.entity.plant.PlantCovering;
import io.github.finalwave.model.game.entity.projectile.ProjectileEffect;
import io.github.finalwave.model.game.entity.projectile.ProjectileProfile;
import io.github.finalwave.model.game.entity.projectile.Projectile;
import io.github.finalwave.model.game.entity.zombie.ArcadeObstacle;
import io.github.finalwave.model.game.entity.zombie.PianoObstacle;
import io.github.finalwave.model.game.entity.zombie.Zombie;
import io.github.finalwave.model.item.SunType;

import java.util.ArrayList;
import java.util.List;

public interface GameContext {

    int getCurrentTick();

    int getTicksPerSecond();

    int getRowCount();

    int getColCount();

    default Tile getTileAt(int col, int row) {
        return null;
    }

    default boolean areZombiesImmuneToChill() {
        return false;
    }

    default boolean lockZombieLanes() {
        return false;
    }

    Plant getPlantAt(int col, int row);

    Plant getPlantInFront(double zombieX, int row);

    List<Zombie> getZombiesInRow(int row);

    List<Zombie> getAllZombies();

    void spawnProjectile(int row, double startX, int damage, String projectileType);

    default void spawnProjectile(Zombie source, int row, double startX,
                                 int damage, String projectileType) {
        spawnProjectile(row, startX, damage, projectileType);
    }

    default void reflectProjectile(Zombie reflector, Projectile projectile) {
    }

    Zombie spawnZombieOfType(String alias, int row, double x);

    void onZombieReachedHouse(Zombie zombie);

    default boolean zombiesWalkOffLawn() {
        return false;
    }

    default void despawnWalkOffZombie(Zombie zombie) {
    }

    void onZombieKilled(Zombie zombie);

    void onPlantDestroyed(Plant plant);

    void applyRowEffect(int row, String effectType, int durationTicks);

    void spawnProjectile(Plant plant, int damage, int shots, ProjectileProfile profile);

    default void spawnReverseProjectile(Plant plant, int damage, int shots, ProjectileProfile profile) {
        spawnProjectile(plant, damage, shots, profile);
    }

    default void spawnDirectedProjectile(Plant plant, int damage, double vx, double vy, float visualScale) {
        spawnDirectedProjectile(plant, damage, vx, vy, visualScale, 0, 0);
    }

    default void spawnDirectedProjectile(Plant plant, int damage, double vx, double vy, float visualScale,
                                         double laneOffset, double extraX) {
        spawnProjectile(plant, damage, 1, ProjectileProfile.straight());
    }

    default void spawnDirectedProjectile(Plant plant, int damage, double vx, double vy, float visualScale,
                                         double laneOffset, double extraX, ProjectileEffect effect) {
        spawnDirectedProjectile(plant, damage, vx, vy, visualScale, laneOffset, extraX);
    }

    default void spawnPoisonLaneBall(Plant plant, int damage) {
        spawnLaneClearProjectile(plant, damage, ProjectileEffect.POISON);
    }

    default void addGooLaneTrail(Plant plant, int durationTicks) {
    }

    default void addGooPuddle(int col, int row, int durationTicks) {
    }

    default List<GooPuddle> getGooPuddles() {
        return List.of();
    }

    void boostFamily(Plant plant, PlantCategory boostedFamily, double extendedDuration);

    void resetFamilyCooldowns(PlantCategory boostedFamily);

    void spawnSun(Plant plant, double total);

    default void spawnSun(Plant plant, double total, SunType type) {
        spawnSun(plant, total);
    }

    default void spawnSunAt(int col, int row, int value, SunType type) {
    }

    void armTrap(Plant plant);

    void explode(Plant plant, int damage, double radius);

    default void clearGraveAt(int col, int row) {
    }

    void applyFieldModifier(Plant plant, double magnitude);

    void grantArmor(Plant plant, int armorValue);

    void dealMeleeDamage(Plant plant, int damage, boolean areaOfEffect);

    void projectileBurst(Plant plant, double value);

    void hypnotizeRandomZombies(Plant plant, int value);

    void freezeAllZombies(Plant plant, double value);

    void knockbackBlast(Plant plant);

    void spawnClones(Plant plant, int value);

    void pullUnderwater(Plant plant, double value);

    void localAreaAttack(Plant plant, double value);

    default void spawnLaneClearProjectile(Plant plant, int damage, ProjectileEffect effect) {
        spawnProjectile(plant, damage, 1, ProjectileProfile.piercingProfile());
    }

    default void spawnBowlingProjectile(Plant plant, int damage, ProjectileEffect effect,
                                      ProjectileProfile profile) {
        spawnProjectile(plant, damage, 1, profile);
    }

    default void spawnPiercingProjectile(Plant plant, int damage, ProjectileEffect effect, int pierce) {
        spawnProjectile(plant, damage, 1, ProjectileProfile.piercingProfile());
    }

    default List<Plant> getAllPlants() {
        ArrayList<Plant> plants = new java.util.ArrayList<>();
        for (int row = 0; row < getRowCount(); row++) {
            for (int col = 0; col < getColCount(); col++) {
                Plant plant = getPlantAt(col, row);
                if (plant != null && plant.isAlive()) {
                    plants.add(plant);
                }
            }
        }
        return plants;
    }

    default boolean movePlant(Plant plant, int col, int row) {
        return false;
    }

    default void createGraves(int count) {
    }

    default int withdrawSun(int amount) {
        return 0;
    }

    default void returnSun(int amount) {
    }

    default int stealGroundSun(int maximum) {
        return stealGroundSun(null, maximum);
    }

    default int stealGroundSun(Zombie thief, int maximum) {
        return 0;
    }

    default boolean isWaterAt(int col, int row) {
        return false;
    }

    default void pushIceInRow(int row) {
    }

    default void pushIceAhead(Zombie zombie) {
        if (zombie != null) {
            pushIceInRow(zombie.getRow());
        }
    }

    default void createIceBlocks(int row, int startCol, int count) {
    }

    default boolean damageGraveAt(int col, int row, int amount) {
        return false;
    }

    default boolean damageIceAt(int col, int row, int amount) {
        return false;
    }

    default PlantCovering coverPlant(Plant plant, PlantCovering.Type type, int health) {
        return coverPlant(plant, type, health, null);
    }

    default PlantCovering coverPlant(Plant plant, PlantCovering.Type type, int health, Zombie source) {
        return null;
    }

    default void registerHunterIceHit(Plant plant) {
    }

    default List<PlantCovering> getPlantCoverings() {
        return List.of();
    }

    default int throwTombBones(Zombie source, int count, int maxGraves) {
        return 0;
    }

    default boolean canThrowTombBones(Zombie source, int count, int maxGraves) {
        return false;
    }

    default void queuePlantBurn(int col, int row) {
    }

    default void fireLaneLaser(int row, int fromCol, int span) {
    }

    default void fireLaneLaser(int row, int fromCol, int span, int delayTicks) {
        fireLaneLaser(row, fromCol, span, delayTicks, fromCol + 0.5);
    }

    default void fireLaneLaser(int row, int fromCol, int span, int delayTicks, double originX) {
        fireLaneLaser(row, fromCol, span);
    }

    default void pushArcadeObstacle(Zombie pusher) {
    }

    default void releaseArcadeObstacle(String pusherId) {
    }

    default List<ArcadeObstacle> getArcadeObstacles() {
        return List.of();
    }

    default void pushPianoObstacle(Zombie pusher) {
    }

    default void releasePianoObstacle(String pusherId) {
    }

    default List<PianoObstacle> getPianoObstacles() {
        return List.of();
    }

    default void dropSeedPacket(String plantName, int col, int row) {
    }
}