package model.game;

import model.game.entity.plant.Plant;
import model.game.entity.zombie.Zombie;
import model.item.Sun;
import model.item.SunType;

import java.util.List;

public interface MatchListener {

    default void onSunProduced(Plant plant, int x, int y) {
    }

    default void onSunDropped(SunType type, int x, int y) {
    }

    default void onSunReachedGround(int x, int y) {
    }

    default void onPlantDestroyed(Plant plant, int x, int y) {
    }

    default void onLawnMowerTriggered(int row, List<Zombie> killed) {
    }

    default void onLawnMowerFailed(int row) {
    }

    default void onWaveStarted(int waveNumber) {
    }

    default void onFinalWave() {
    }

    default void onZombieSpawned(String type, int wave, int lane, int cost) {
    }

    default void onZombieDied(String type, double x, double y) {
    }

    default void onGlowingZombieDroppedFood(int plantFoodCount) {
    }

    default void onItemDropped(String itemType, int newCount) {
    }

    default void onGraveCreated(int col, int row, String lootType) {
    }

    default void onWin() {
    }

    default void onLose() {
    }

    default void onRadioactiveSunExploded(int x, int y) {
    }

    default void onConveyorBeltPlantArrived(String plantName) {
    }
}
