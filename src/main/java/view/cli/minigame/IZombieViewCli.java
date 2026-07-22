package view.cli.minigame;

import model.game.entity.zombie.Zombie;
import view.api.minigame.IZombieView;
import view.cli.CliView;

import java.util.List;
import java.util.Map;

public class IZombieViewCli extends CliView implements IZombieView {

    @Override
    public void showStageStarted(int stageIndex, int placementColumn, int startingSun) {
        displayMessage("I, Zombie stage " + stageIndex
                + " started. Place zombies right of column " + placementColumn
                + ". Starting sun: " + startingSun + ".");
    }

    @Override
    public void showRoster(List<String> names, Map<String, Integer> costs) {
        if (names == null || names.isEmpty()) {
            displayMessage("Zombie roster: (empty)");
            return;
        }
        StringBuilder sb = new StringBuilder("Zombie roster:");
        for (String name : names) {
            int cost = costs == null ? 0 : costs.getOrDefault(name, 0);
            sb.append("\n  ").append(name).append(" cost=").append(cost);
        }
        displayMessage(sb.toString());
    }

    @Override
    public void showZombiePlaced(String name, int col, int row) {
        displayMessage("Placed " + name + " at (" + col + ", " + row + ")");
    }

    @Override
    public void showBrainEaten(int row) {
        displayMessage("A zombie ate the brain on row " + (row + 1) + "!");
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
                    + " at (" + zombie.getX() + ", " + zombie.getRow() + ")"
                    + (zombie.isStationary() ? " [sun-producer]" : ""));
        }
    }

    @Override
    public void showWinMessage() {
        displayMessage("You won the I, Zombie stage! All brains eaten.");
    }

    @Override
    public void showLoseMessage() {
        displayMessage("You lost the I, Zombie stage. Not enough sun and no zombies left.");
    }

    @Override
    public void errorInvalidCommand() {
        displayError("Invalid I, Zombie command.");
    }

    @Override
    public void errorInvalidLocation(int col, int row) {
        displayError("Invalid location: (" + col + ", " + row + ")");
    }

    @Override
    public void errorBeyondPlantingLine(int col, int row, int placementColumn) {
        displayError("Cannot place at (" + col + ", " + row
                + "). Must place to the right of column " + placementColumn + ".");
    }

    @Override
    public void errorNotInRoster(String type) {
        displayError("Zombie " + type + " is not in this stage's roster.");
    }

    @Override
    public void errorInsufficientSun(String type, int cost, int balance) {
        displayError("Not enough sun to place " + type
                + " (cost=" + cost + ", balance=" + balance + ").");
    }

    @Override
    public void errorUnknownZombie(String type) {
        displayError("Unknown zombie type: " + type);
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
