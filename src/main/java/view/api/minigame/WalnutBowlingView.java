package view.api.minigame;

import model.game.entity.zombie.Zombie;
import model.minigame.bowling.BowlingNutType;
import view.api.View;

import java.util.List;

public interface WalnutBowlingView extends View {

    void showStageStarted(int stageIndex, int redLineColumn);

    void showConveyorBelt(List<String> plantsOnBelt);

    void showConveyorBeltPlantArrived(String plantName);

    void showBowlingNutSpawned(String plantName, int col, int row);

    void showBowlingNutHit(BowlingNutType type, String zombieType, double x, double row);

    void showBowlingNutExploded(int col, int row);

    void showAdvanceTime(int ticks);

    void showMap(String mapRepresentation);

    void showZombiesInfo(List<Zombie> zombies);

    void showWinMessage();

    void showLoseMessage();

    void errorInvalidCommand();

    void errorInvalidLocation(int col, int row);

    void errorCannotPlantHere(int col, int row);

    void errorBeyondPlantingLine(int col, int row, int redLineColumn);

    void errorPlantNotOnConveyorBelt(String type);

    void errorUnknownPlant(String type);

    void errorInvalidTickCount();

    void errorNegativeTickCount();
}
