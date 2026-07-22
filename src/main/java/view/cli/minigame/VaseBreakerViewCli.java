package view.cli.minigame;

import model.game.entity.Vase;
import model.game.entity.zombie.Zombie;
import view.api.minigame.VaseBreakerView;
import view.cli.CliView;

import java.util.List;

public class VaseBreakerViewCli extends CliView implements VaseBreakerView {

    @Override
    public void showStageStarted(int stageIndex) {
        displayMessage("Vasebreaker stage " + stageIndex + " started. Smash all vases!");
    }

    @Override
    public void showVaseSmashed(int col, int row, Vase.Content content) {
        displayMessage("Vase smashed at (" + col + ", " + row + ") — content: " + content);
    }

    @Override
    public void showSeedPacketDropped(String plantName, int col, int row) {
        displayMessage("Seed packet dropped: " + plantName + " at (" + col + ", " + row + ")");
    }

    @Override
    public void showSeedPacketExpired(String plantName, int col, int row) {
        displayMessage("Seed packet expired: " + plantName + " at (" + col + ", " + row + ")");
    }

    @Override
    public void showSeedPacketPlanted(String plantName, int col, int row) {
        displayMessage("Planted " + plantName + " from seed packet at (" + col + ", " + row + ")");
    }

    @Override
    public void showZombieSpawned(String type, double x, int row) {
        displayMessage("Zombie spawned: " + type + " at (" + x + ", " + row + ")");
    }

    @Override
    public void showZombieDied(String type, double x, double y) {
        displayMessage("Zombie died: " + type + " at (" + x + ", " + y + ")");
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
    public void showNukeActivated() {
        displayMessage("Nuke activated. All zombies destroyed.");
    }

    @Override
    public void showWinMessage() {
        displayMessage("You won the Vasebreaker stage!");
    }

    @Override
    public void showLoseMessage() {
        displayMessage("You lost the Vasebreaker stage.");
    }

    @Override
    public void errorInvalidCommand() {
        displayError("Invalid vasebreaker command.");
    }

    @Override
    public void errorNoVaseAt(int col, int row) {
        displayError("No vase at (" + col + ", " + row + ")");
    }

    @Override
    public void errorNoSeedPacketAt(int col, int row) {
        displayError("No seed packet at (" + col + ", " + row + ")");
    }

    @Override
    public void errorCannotPlantHere(int col, int row) {
        displayError("Cannot plant seed packet at (" + col + ", " + row + ")");
    }

    @Override
    public void errorInvalidLocation(int col, int row) {
        displayError("Invalid location: (" + col + ", " + row + ")");
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
