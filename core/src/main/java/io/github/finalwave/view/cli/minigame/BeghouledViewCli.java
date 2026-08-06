package io.github.finalwave.view.cli.minigame;

import io.github.finalwave.model.game.entity.zombie.Zombie;
import io.github.finalwave.model.minigame.beghouled.BeghouledUpgradeRule;
import io.github.finalwave.view.api.minigame.BeghouledView;
import io.github.finalwave.view.cli.CliView;

import java.util.List;

public class BeghouledViewCli extends CliView implements BeghouledView {

    @Override
    public void showStageStarted(int stageIndex, int matchTarget, List<String> plantPool) {
        displayMessage("Beghouled stage " + stageIndex
                + " started. Make " + matchTarget + " matches to win.");
        displayMessage("Plant pool: " + String.join(", ", plantPool));
    }

    @Override
    public void showUpgrades(List<BeghouledUpgradeRule> upgrades) {
        if (upgrades == null || upgrades.isEmpty()) {
            displayMessage("No upgrades available.");
            return;
        }
        StringBuilder sb = new StringBuilder("Upgrades:");
        for (BeghouledUpgradeRule rule : upgrades) {
            sb.append("\n  ").append(rule.fromPlant())
                    .append(" -> ").append(rule.toPlant())
                    .append(" cost=").append(rule.sunCost());
        }
        displayMessage(sb.toString());
    }

    @Override
    public void showSwapAccepted(int matchesCleared, int sunAwarded) {
        displayMessage("Swap resolved: " + matchesCleared
                + " match(es), +" + sunAwarded + " sun.");
    }

    @Override
    public void showBoardReset() {
        displayMessage("No more valid moves. The board was reset.");
    }

    @Override
    public void showUpgradeApplied(String fromPlant, String toPlant,
                                   int plantsConverted, int sunSpent) {
        displayMessage("Upgraded " + plantsConverted + " " + fromPlant
                + " to " + toPlant + " (-" + sunSpent + " sun).");
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
        displayMessage("You won Beghouled! Match target reached.");
    }

    @Override
    public void showLoseMessage() {
        displayMessage("You lost Beghouled.");
    }

    @Override
    public void showCurrentMenu() {
        displayMessage("Current menu: beghouled");
    }

    @Override
    public void errorInvalidCommand() {
        displayError("Invalid Beghouled command.");
    }

    @Override
    public void errorSwapOutOfBounds() {
        displayError("Swap locations are out of bounds.");
    }

    @Override
    public void errorSwapNotAdjacent() {
        displayError("Plants must be adjacent to swap.");
    }

    @Override
    public void errorSwapNoMatch() {
        displayError("That swap does not create a match.");
    }

    @Override
    public void errorSwapMissingPlant() {
        displayError("Both cells must have plants to swap.");
    }

    @Override
    public void errorSwapCraterBlocked() {
        displayError("Cannot swap onto a crater.");
    }

    @Override
    public void errorUpgradeUnknown(String plantName) {
        displayError("No upgrade available for " + plantName + ".");
    }

    @Override
    public void errorUpgradeInsufficientSun(int cost, int balance) {
        displayError("Not enough sun for upgrade (cost=" + cost
                + ", balance=" + balance + ").");
    }

    @Override
    public void errorUpgradeNoPlants(String plantName) {
        displayError("No " + plantName + " plants on the board to upgrade.");
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
