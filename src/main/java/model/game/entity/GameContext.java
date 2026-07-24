package model.game.entity;

import model.game.board.tile.Tile;
import model.game.entity.plant.Plant;
import model.game.entity.plant.PlantCategory;
import model.game.entity.plant.PlantCovering;
import model.game.entity.projectile.ProjectileProfile;
import model.game.entity.projectile.Projectile;
import model.game.entity.zombie.ArcadeObstacle;
import model.game.entity.zombie.Zombie;

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

    void spawnZombieOfType(String alias, int row, double x);

    void onZombieReachedHouse(Zombie zombie);

    void onZombieKilled(Zombie zombie);

    void onPlantDestroyed(Plant plant);

    void applyRowEffect(int row, String effectType, int durationTicks);

    void spawnProjectile(Plant plant, int damage, int shots, ProjectileProfile profile);

    void boostFamily(Plant plant, PlantCategory boostedFamily, double extendedDuration);

    void resetFamilyCooldowns(PlantCategory boostedFamily);

    void spawnSun(Plant plant, double total);

    void armTrap(Plant plant);

    void explode(Plant plant, int damage, double radius);

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
        return 0;
    }

    default boolean isWaterAt(int col, int row) {
        return false;
    }

    default void pushIceInRow(int row) {
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
        return null;
    }

    default void registerHunterIceHit(Plant plant) {
    }

    default List<PlantCovering> getPlantCoverings() {
        return List.of();
    }

    default void pushArcadeObstacle(Zombie pusher) {
    }

    default void releaseArcadeObstacle(String pusherId) {
    }

    default List<ArcadeObstacle> getArcadeObstacles() {
        return List.of();
    }

    default void dropSeedPacket(String plantName, int col, int row) {
    }
}