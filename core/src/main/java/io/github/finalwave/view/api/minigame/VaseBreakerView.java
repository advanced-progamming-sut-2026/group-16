package io.github.finalwave.view.api.minigame;

import io.github.finalwave.model.game.entity.Vase;
import io.github.finalwave.model.game.entity.zombie.Zombie;
import io.github.finalwave.view.api.View;

import java.util.List;

public interface VaseBreakerView extends View {

    void showStageStarted(int stageIndex);

    void showVaseSmashed(int col, int row, Vase.Content content);

    void showSeedPacketDropped(String plantName, int col, int row);

    void showSeedPacketExpired(String plantName, int col, int row);

    void showSeedPacketPlanted(String plantName, int col, int row);

    void showZombieSpawned(String type, double x, int row);

    void showZombieDied(String type, double x, double y);

    void showAdvanceTime(int ticks);

    void showMap(String mapRepresentation);

    void showZombiesInfo(List<Zombie> zombies);

    void showNukeActivated();

    void showWinMessage();

    void showLoseMessage();

    void errorInvalidCommand();

    void errorNoVaseAt(int col, int row);

    void errorNoSeedPacketAt(int col, int row);

    void errorCannotPlantHere(int col, int row);

    void errorInvalidLocation(int col, int row);

    void errorInvalidTickCount();

    void errorNegativeTickCount();
}
