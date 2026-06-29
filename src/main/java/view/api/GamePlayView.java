package view.api;

import model.game.entity.Plant;
import model.game.entity.zombie.Zombie;
import model.item.Sun;

import java.util.List;

public interface GamePlayView {

    void showAdvanceTime(int ticks);

    void showSunProduced(Plant plant, int x, int y);

    void showSunDropped(Sun sun, int x, int y);

    void showSunReachedGround(int x, int y);

    void showSunAmount(int amount);

    void showCheatAddedSuns(int suns);

    void showPlantPlanted(String plantType, int x, int y);

    void showPlantPlucked(int x, int y);

    void showPlantDestroyed(Plant plant, int x, int y);

    void showLawnMowerTriggered(int row);

    void showLawnMowerFailed(int row);

    void showWaveStarted(int waveNumber);

    void showFinalWave();

    void showZombieSpawned(String zombieType, int wave, int lane, int cost);

    void showZombieDied(String zombieType, double x, double y);

    void showNukeActivated();

    void showCheatCooldownRemoved();

    void showGlowingZombieDroppedFood(int currentFoods);

    void showPlantFed(int x, int y);

    void showCheatAddedPlantFood();

    void showWinMessage();

    void showMap(String mapRepresentation);

    void showPlantsStatus(String status);

    void showTileStatus(String tileInfo);

    void showItemDropped(String itemType, int count);

    void showZombiesInfo(List<Zombie> zombies);

    void showCheatSpawnZombie(String zombieType, double x, double y);

    // ====================== ERROR METHODS ======================

    void errorPlantNotSelected(String type);

    void errorPlantAlreadySelected(String type);

    void errorPlantLocked(String type);

    void errorPlantNotFound(String type);

    void errorNoPlantToRemove(String type);

    void errorNotEnoughSun();

    void errorInvalidLocation(int x, int y);

    void errorCannotPlantHere(int x, int y);

    void errorPlantOnCooldown(String type);

    void errorNoPlantToPluck(int x, int y);

    void errorNoPlantFood();

    void errorCannotFeedHere(int x, int y);

    void errorCannotBoostPlant(String type);

    void errorNotEnoughDiamonds();

    void errorGameNotStarted();
}