package view.cli.minigame;

import model.game.entity.zombie.Zombie;
import model.minigame.bowling.BowlingNutType;
import view.api.minigame.WalnutBowlingView;
import view.cli.CliView;

import java.util.List;
import java.util.Locale;

public class WalnutBowlingViewCli extends CliView implements WalnutBowlingView {

    @Override
    public void showStageStarted(int stageIndex, int redLineColumn) {
        displayMessage("Wallnut Bowling stage " + stageIndex
                + " started. Plant nuts from the conveyor belt up to column " + redLineColumn + ".");
    }

    @Override
    public void showConveyorBelt(List<String> plantsOnBelt) {
        if (plantsOnBelt == null || plantsOnBelt.isEmpty()) {
            displayMessage("Conveyor belt: (empty)");
            return;
        }
        displayMessage("Conveyor belt nuts ready to plant: " + String.join(", ", plantsOnBelt));
    }

    @Override
    public void showConveyorBeltPlantArrived(String plantName) {
        displayMessage("The conveyor belt brought a " + plantName + "!");
    }

    @Override
    public void showBowlingNutSpawned(String plantName, int col, int row) {
        displayMessage("Launched " + plantName + " at (" + col + ", " + row + ")");
    }

    @Override
    public void showBowlingNutHit(BowlingNutType type, String zombieType, double x, double row) {
        displayMessage("Bowling nut (" + type + ") hit " + zombieType
                + " at (" + String.format(Locale.US, "%.1f", x) + ", "
                + String.format(Locale.US, "%.1f", row) + ")");
    }

    @Override
    public void showBowlingNutExploded(int col, int row) {
        displayMessage("Explode-o-nut detonated at (" + col + ", " + row + ")");
    }

    @Override
    public void showAdvanceTime(int ticks) {
        displayMessage("Advanced time by " + ticks + " ticks.");
    }

    @Override
    public void showMap(String mapRepresentation) {
        displayMessage(mapRepresentation);
    }

    @Override
    public void showZombiesInfo(List<Zombie> zombies) {
        if (zombies == null || zombies.isEmpty()) {
            displayMessage("No zombies on the board.");
            return;
        }
        for (Zombie zombie : zombies) {
            displayMessage(zombie.getType() + " HP=" + zombie.getHealth()
                    + " at (" + zombie.getX() + ", " + zombie.getRow() + ")");
        }
    }

    @Override
    public void showWinMessage() {
        displayMessage("You won the Wallnut Bowling stage!");
    }

    @Override
    public void showLoseMessage() {
        displayMessage("You lost the Wallnut Bowling stage.");
    }

    @Override
    public void errorInvalidCommand() {
        displayError("Invalid walnut bowling command.");
    }

    @Override
    public void errorInvalidLocation(int col, int row) {
        displayError("Invalid location: (" + col + ", " + row + ")");
    }

    @Override
    public void errorCannotPlantHere(int col, int row) {
        displayError("Cannot plant here at (" + col + ", " + row + ")");
    }

    @Override
    public void errorBeyondPlantingLine(int col, int row, int redLineColumn) {
        displayError("Cannot plant at (" + col + ", " + row + "). Red line is at column " + redLineColumn + ".");
    }

    @Override
    public void errorPlantNotOnConveyorBelt(String type) {
        displayError("Nut " + type + " is not on the conveyor belt yet.");
    }

    @Override
    public void errorUnknownPlant(String type) {
        displayError("Unknown nut type: " + type);
    }

    @Override
    public void errorInvalidTickCount() {
        displayError("Invalid tick count.");
    }

    @Override
    public void errorNegativeTickCount() {
        displayError("Tick count must not be negative.");
    }
}
