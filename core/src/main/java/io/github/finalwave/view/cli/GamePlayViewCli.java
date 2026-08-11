package io.github.finalwave.view.cli;

import io.github.finalwave.model.game.entity.plant.Plant;
import io.github.finalwave.model.game.entity.zombie.Zombie;
import io.github.finalwave.model.item.Sun;
import io.github.finalwave.view.api.GamePlayView;

import java.util.List;

public class GamePlayViewCli extends CliView implements GamePlayView {


    @Override
    public void showAdvanceTime(int ticks) {
        displayMessage("The game advanced " + ticks + " tick(s).");
    }


    @Override
    public void showSunProduced(Plant plant, int x, int y) {
        displayMessage("plant " + plant.getName() + " produced a sun at (" + x + ", " + y + ")");
    }


    @Override
    public void showSunDropped(Sun sun, int x, int y) {
        displayMessage("New " + sun.getType().name().toLowerCase()
                + " sun is dropping at position (" + x + ", " + y + ")");
    }


    @Override
    public void showSunReachedGround(int x, int y) {
        displayMessage("Sun reached the ground at position (" + x + ", " + y + ")");
    }


    @Override
    public void showSunAmount(int amount) {
        displayMessage("You have " + amount + " sun(s).");
    }


    @Override
    public void showCheatAddedSuns(int suns) {
        displayMessage("CHEAT: You got " + suns + " sun(s).");
    }


    @Override
    public void showPlantPlanted(String plantType, int x, int y) {
        displayMessage("Planted " + plantType + " at (" + x + ", " + y + ")");
    }


    @Override
    public void showPlantPlucked(int x, int y) {
        displayMessage("Plucked plant at (" + x + ", " + y + ")");
    }


    @Override
    public void showPlantDestroyed(Plant plant, int x, int y) {
        displayMessage("Plant " + plant.getName() + " at (" + x + ", " + y + ") is destroyed.");
    }


    @Override
    public void showLawnMowerTriggered(int row) {
        displayMessage("The lawn mower in the row " + row + " is triggered and killed these zombies:");
    }


    @Override
    public void showLawnMowerFailed(int row) {
        displayMessage("The zombie ate your brain; LOSER!!!");
    }


    @Override
    public void showWaveStarted(int waveNumber) {
        displayMessage("Wave " + waveNumber + " started.");
    }


    @Override
    public void showFinalWave() {
        displayMessage("The final wave has come.");
    }


    @Override
    public void showZombieSpawned(String zombieType, int wave, int lane, int cost) {
        displayMessage("Zombie " + zombieType + " spawned at wave "
                + wave + " in lane " + lane + " which costed " + cost + ".");
    }


    @Override
    public void showZombieDied(String zombieType, double x, double y) {
        displayMessage("Zombie of type " + zombieType
                + " is dead at (" + String.format("%.1f", x) + ", " + (int) y + ")");
    }


    @Override
    public void showNukeActivated() {
        displayMessage("CHEAT: Nuke released! All zombies have been destroyed.");
    }


    @Override
    public void showCheatCooldownRemoved() {
        displayMessage("CHEAT: All plant cooldowns have been removed.");
    }


    @Override
    public void showGlowingZombieDroppedFood(int currentFoods) {
        displayMessage("The glowing zombie dropped a plant food; you have " + currentFoods + " plant foods now.");
    }


    @Override
    public void showPlantFed(int x, int y) {
        displayMessage("Fed plant at (" + x + ", " + y + ")");
    }


    @Override
    public void showCheatAddedPlantFood() {
        displayMessage("CHEAT: Added one plant food.");
    }


    @Override
    public void showWinMessage() {
        displayMessage("Dear humanz, zis is not done yet; we will come back to eat your brainz, humanz.");
    }


    @Override
    public void showMap(String mapRepresentation) {
        System.out.println(mapRepresentation);
    }


    @Override
    public void showPlantsStatus(String status) {
        System.out.println(status);
    }


    @Override
    public void showTileStatus(String tileInfo) {
        System.out.println(tileInfo);
    }


    @Override
    public void showItemDropped(String itemType, int count) {
        String plural = (count > 1) ? "s" : "";
        displayMessage("A zombie dropped a " + itemType + "; you have " + count + " " + itemType + plural + " now.");
    }


    @Override
    public void showZombiesInfo(List<Zombie> zombies) {
        if (zombies == null || zombies.isEmpty()) {
            displayMessage("No zombies on the board.");
            return;
        }
        for (Zombie zombie : zombies) {
            displayMessage(zombie.getType() + ":");
            displayMessage("position: " + String.format("%.1f", zombie.getX())
                    + ", " + zombie.getRow());
            displayMessage("health: " + zombie.getHealth());
            displayMessage("armor:");
            for (var armor : zombie.getArmorLayers()) {
                if (!armor.isDestroyed()) {
                    displayMessage(armor.getType() + ": " + armor.getHealth());
                }
            }
            displayMessage("effects:");
            if (zombie.getFreezeTicksRemaining() > 0) {
                displayMessage("frozen: "
                        + String.format("%.1fs", zombie.getFreezeTicksRemaining() / 10.0));
            }
        }
    }


    @Override
    public void showCheatSpawnZombie(String zombieType, double x, double y) {
        displayMessage("Zombie of type " + zombieType
                + " is spawned at (" + String.format("%.1f", x) + ", " + (int) y + ")");
    }


    @Override
    public void errorPlantNotSelected(String type) {
        displayError("Plant " + type + " has not been selected for this level.");
    }

    @Override
    public void errorPlantNotOnConveyorBelt(String type) {
        displayError("Plant " + type + " has not arrived on the conveyor belt yet.");
    }

    @Override
    public void errorLevelPlantLocked(String type) {
        displayError("Plant " + type + " is locked for this level.");
    }

    @Override
    public void errorPlantAlreadySelected(String type) {
        displayError("Plant " + type + " is already selected.");
    }

    @Override
    public void errorPlantLocked(String type) {
        displayError("Plant " + type + " is locked.");
    }

    @Override
    public void errorPlantNotFound(String type) {
        displayError("Plant " + type + " does not exist.");
    }

    @Override
    public void errorNoPlantToRemove(String type) {
        displayError("Plant " + type + " is not selected to be removed.");
    }


    @Override
    public void errorNotEnoughSun() {
        displayError("You don't have enough sun to plant this.");
    }

    @Override
    public void errorInvalidLocation(int x, int y) {
        displayError("Invalid location (" + x + ", " + y + ").");
    }

    @Override
    public void errorCannotPlantHere(int x, int y) {
        displayError("You cannot plant here at (" + x + ", " + y + ").");
    }

    @Override
    public void errorPlantOnCooldown(String type) {
        displayError("Plant " + type + " is on cooldown.");
    }


    @Override
    public void errorNoPlantToPluck(int x, int y) {
        displayError("There is no plant to pluck at (" + x + ", " + y + ").");
    }

    @Override
    public void errorCannotPluckProtectedSeed(int x, int y) {
        displayError("Cannot pluck a protected seed at (" + x + ", " + y + ").");
    }


    @Override
    public void errorNoPlantFood() {
        displayError("You don't have any plant food left.");
    }

    @Override
    public void errorCannotFeedHere(int x, int y) {
        displayError("There is no plant to feed at (" + x + ", " + y + ").");
    }


    @Override
    public void errorCannotBoostPlant(String type) {
        displayError("Cannot boost plant " + type + ".");
    }

    @Override
    public void errorNotEnoughDiamonds() {
        displayError("You don't have enough diamonds to boost this plant.");
    }


    @Override
    public void errorGameNotStarted() {
        displayError("Please start the game first using 'start game'.");
    }

    @Override
    public void errorInvalidCommand() {
        displayError("Invalid gameplay command.");
    }

    @Override
    public void errorInvalidTickCount() {
        displayError("Invalid tick count.");
    }

    @Override
    public void errorNegativeTickCount() {
        displayError("Tick count must be non-negative.");
    }

    @Override
    public void errorNoSunAt(int col, int row) {
        displayError("No sun at (" + col + ", " + row + ").");
    }

    @Override
    public void errorInvalidSunCount() {
        displayError("Invalid sun count.");
    }

    @Override
    public void errorInvalidZombieLocation() {
        displayError("Invalid zombie location.");
    }

    @Override
    public void errorZombieSpawnFailed(String message) {
        displayError(message);
    }

    @Override
    public void showLawnMowerKilledZombie(String zombieType) {
        displayMessage("- " + zombieType);
    }

    @Override
    public void showGraveCreated(int col, int row, String lootType) {
        String detail = "NONE".equals(lootType) ? "" : " (loot: " + lootType + ")";
        displayMessage("A grave appeared at (" + col + ", " + row + ")" + detail + ".");
    }
}