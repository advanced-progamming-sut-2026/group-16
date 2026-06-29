package model.game.entity;

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
}