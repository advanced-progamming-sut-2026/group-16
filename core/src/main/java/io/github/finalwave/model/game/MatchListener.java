package io.github.finalwave.model.game;

import io.github.finalwave.model.game.entity.Vase;
import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.zombie.Zombie;
import io.github.finalwave.model.item.SunType;
import io.github.finalwave.model.minigame.bowling.BowlingNutType;

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

    default void onProtectedSeedDestroyed(Plant plant, int x, int y) {
    }

    default void onTimedWarTimeUp() {
    }

    default void onTimedWarGoalReached(TimedWarMode mode, int progress) {
    }

    default void onDeadLineBreached(int column, String zombieType) {
    }

    default void onLoveYourPlantsLimitReached(int plantsLost, int maxAllowed) {
    }

    default void onPlantWhatYouGetWavesStarted() {
    }

    default void onVaseSmashed(int col, int row, Vase.Content content) {
    }

    default void onSeedPacketDropped(String plantName, int col, int row) {
    }

    default void onSeedPacketExpired(String plantName, int col, int row) {
    }

    default void onSeedPacketPlanted(String plantName, int col, int row) {
    }

    default void onBowlingNutSpawned(String plantName, int col, int row) {
    }

    default void onBowlingNutHit(BowlingNutType type, String zombieType,
                                 double x, double row) {
    }

    default void onBowlingNutExploded(int col, int row) {
    }

    default void onBrainEaten(int row) {
    }
}
