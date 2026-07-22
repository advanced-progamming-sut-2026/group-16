package view.cli.minigame;

import model.game.entity.zombie.Zombie;
import view.api.minigame.ZombotanyView;
import view.cli.CliView;

import java.util.List;

public class ZombotanyViewCli extends CliView implements ZombotanyView {

    @Override
    public void showStageStarted(int stageIndex, int startingSun, List<String> plantPool) {
        displayMessage("Zombotany stage " + stageIndex
                + " started. Starting sun: " + startingSun + ".");
        displayMessage("Plant pool: " + String.join(", ", plantPool));
    }

    @Override
    public void showSunAmount(int amount) {
        displayMessage("Sun: " + amount);
    }

    @Override
    public void showPlantPlanted(String plantType, int col, int row) {
        displayMessage("Planted " + plantType + " at (" + col + ", " + row + ").");
    }

    @Override
    public void showPlantPlucked(int col, int row) {
        displayMessage("Plucked plant at (" + col + ", " + row + ").");
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
        displayMessage("You won Zombotany! All waves cleared.");
    }

    @Override
    public void showLoseMessage() {
        displayMessage("You lost Zombotany.");
    }

    @Override
    public void showCurrentMenu() {
        displayMessage("Current menu: zombotany");
    }

    @Override
    public void errorInvalidCommand() {
        displayError("Invalid Zombotany command.");
    }

    @Override
    public void errorInvalidLocation(int col, int row) {
        displayError("Invalid location (" + col + ", " + row + ").");
    }

    @Override
    public void errorPlantNotFound(String plantType) {
        displayError("Unknown plant: " + plantType + ".");
    }

    @Override
    public void errorPlantNotSelected(String plantType) {
        displayError(plantType + " is not in your selected plant pool.");
    }

    @Override
    public void errorPlantOnCooldown(String plantType) {
        displayError(plantType + " is on cooldown.");
    }

    @Override
    public void errorNotEnoughSun() {
        displayError("Not enough sun.");
    }

    @Override
    public void errorCannotPlantHere(int col, int row) {
        displayError("Cannot plant at (" + col + ", " + row + ").");
    }

    @Override
    public void errorNoPlantToPluck(int col, int row) {
        displayError("No plant to pluck at (" + col + ", " + row + ").");
    }

    @Override
    public void errorNoSunAt(int col, int row) {
        displayError("No sun at (" + col + ", " + row + ").");
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
