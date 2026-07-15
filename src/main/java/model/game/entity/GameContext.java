package model.game.entity;

import model.game.entity.plant.Plant;
import model.game.entity.plant.PlantCategory;
import model.game.entity.projectile.ProjectileProfile;
import model.game.entity.zombie.Zombie;

import java.util.List;

public interface GameContext {

    int getCurrentTick();

    int getTicksPerSecond();

    int getRowCount();

    int getColCount();

    Plant getPlantAt(int col, int row);

    Plant getPlantInFront(double zombieX, int row);

    List<Zombie> getZombiesInRow(int row);

    List<Zombie> getAllZombies();

    void spawnProjectile(int row, double startX, int damage, String projectileType);

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
}