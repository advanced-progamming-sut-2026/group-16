package view.api.minigame;

import model.game.entity.zombie.Zombie;
import view.api.View;

import java.util.List;

public interface ZombotanyView extends View {

    void showStageStarted(int stageIndex, int startingSun, List<String> plantPool);

    void showSunAmount(int amount);

    void showPlantPlanted(String plantType, int col, int row);

    void showPlantPlucked(int col, int row);

    void showAdvanceTime(int ticks);

    void showMap(String mapRepresentation);

    void showZombiesInfo(List<Zombie> zombies);

    void showWinMessage();

    void showLoseMessage();

    void showCurrentMenu();

    void errorInvalidCommand();

    void errorInvalidLocation(int col, int row);

    void errorPlantNotFound(String plantType);

    void errorPlantNotSelected(String plantType);

    void errorPlantOnCooldown(String plantType);

    void errorNotEnoughSun();

    void errorCannotPlantHere(int col, int row);

    void errorNoPlantToPluck(int col, int row);

    void errorNoSunAt(int col, int row);

    void errorInvalidTickCount();

    void errorNegativeTickCount();
}
