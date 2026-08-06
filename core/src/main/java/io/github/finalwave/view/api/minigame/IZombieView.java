package io.github.finalwave.view.api.minigame;

import io.github.finalwave.model.game.entity.zombie.Zombie;
import io.github.finalwave.view.api.View;

import java.util.List;
import java.util.Map;

public interface IZombieView extends View {

    void showStageStarted(int stageIndex, int placementColumn, int startingSun);

    void showRoster(List<String> names, Map<String, Integer> costs);

    void showZombiePlaced(String name, int col, int row);

    void showBrainEaten(int row);

    void showAdvanceTime(int ticks);

    void showMap(String mapRepresentation);

    void showZombiesInfo(List<Zombie> zombies);

    void showWinMessage();

    void showLoseMessage();

    void errorInvalidCommand();

    void errorInvalidLocation(int col, int row);

    void errorBeyondPlantingLine(int col, int row, int placementColumn);

    void errorNotInRoster(String type);

    void errorInsufficientSun(String type, int cost, int balance);

    void errorUnknownZombie(String type);

    void errorInvalidTickCount();

    void errorNegativeTickCount();
}
