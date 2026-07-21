package view.cli;

import model.game.entity.plant.Plant;
import model.game.entity.zombie.Zombie;
import model.item.Sun;
import view.api.GamePlayView;

import java.util.List;

public class GamePlayViewCli extends CliView implements GamePlayView {

    // advance time command
    @Override
    public void showAdvanceTime(int ticks) {
        displayMessage("The game advanced " + ticks + " tick(s).");
    }

    // sun produced command
    @Override
    public void showSunProduced(Plant plant, int x, int y) {
        displayMessage("plant " + plant.getName() + " produced a sun at (" + x + ", " + y + ")");
    }

    // sun dropped command
    @Override
    public void showSunDropped(Sun sun, int x, int y) {
        displayMessage("New " + sun.getType().name().toLowerCase()
                + " sun is dropping at position (" + x + ", " + y + ")");
    }

    // sun reached ground command
    @Override
    public void showSunReachedGround(int x, int y) {
        displayMessage("Sun reached the ground at position (" + x + ", " + y + ")");
    }

    // show sun amount command
    @Override
    public void showSunAmount(int amount) {
        displayMessage("You have " + amount + " sun(s).");
    }

    // cheat add suns command
    @Override
    public void showCheatAddedSuns(int suns) {
        displayMessage("CHEAT: You got " + suns + " sun(s).");
    }

    // plant planted command
    @Override
    public void showPlantPlanted(String plantType, int x, int y) {
        displayMessage("Planted " + plantType + " at (" + x + ", " + y + ")");
    }

    // plant plucked command
    @Override
    public void showPlantPlucked(int x, int y) {
        displayMessage("Plucked plant at (" + x + ", " + y + ")");
    }

    // plant destroyed command
    @Override
    public void showPlantDestroyed(Plant plant, int x, int y) {
        displayMessage("Plant " + plant.getName() + " at (" + x + ", " + y + ") is destroyed.");
    }

    // lawn mower triggered command
    @Override
    public void showLawnMowerTriggered(int row) {
        displayMessage("The lawn mower in the row " + row + " is triggered and killed these zombies:");
    }

    // lawn mower failed command
    @Override
    public void showLawnMowerFailed(int row) {
        displayMessage("The zombie ate your brain; LOSER!!!");
    }

    // wave started command
    @Override
    public void showWaveStarted(int waveNumber) {
        displayMessage("Wave " + waveNumber + " started.");
    }

    // final wave command
    @Override
    public void showFinalWave() {
        displayMessage("The final wave has come.");
    }

    // zombie spawned command
    @Override
    public void showZombieSpawned(String zombieType, int wave, int lane, int cost) {
        displayMessage("Zombie " + zombieType + " spawned at wave "
                + wave + " in lane " + lane + " which costed " + cost + ".");
    }

    // zombie died command
    @Override
    public void showZombieDied(String zombieType, double x, double y) {
        displayMessage("Zombie of type " + zombieType
                + " is dead at (" + String.format("%.1f", x) + ", " + (int) y + ")");
    }

    // nuke cheat command
    @Override
    public void showNukeActivated() {
        displayMessage("CHEAT: Nuke released! All zombies have been destroyed.");
    }

    // remove cooldown cheat command
    @Override
    public void showCheatCooldownRemoved() {
        displayMessage("CHEAT: All plant cooldowns have been removed.");
    }

    // glowing zombie drop food command
    @Override
    public void showGlowingZombieDroppedFood(int currentFoods) {
        displayMessage("The glowing zombie dropped a plant food; you have " + currentFoods + " plant foods now.");
    }

    // feed plant command
    @Override
    public void showPlantFed(int x, int y) {
        displayMessage("Fed plant at (" + x + ", " + y + ")");
    }

    // cheat add plant food command
    @Override
    public void showCheatAddedPlantFood() {
        displayMessage("CHEAT: Added one plant food.");
    }

    // win message command
    @Override
    public void showWinMessage() {
        displayMessage("Dear humanz, zis is not done yet; we will come back to eat your brainz, humanz.");
    }

    // show map command
    @Override
    public void showMap(String mapRepresentation) {
        System.out.println(mapRepresentation);
    }

    // show plants status command
    @Override
    public void showPlantsStatus(String status) {
        System.out.println(status);
    }

    // show tile status command
    @Override
    public void showTileStatus(String tileInfo) {
        System.out.println(tileInfo);
    }

    // item dropped command
    @Override
    public void showItemDropped(String itemType, int count) {
        String plural = (count > 1) ? "s" : "";
        displayMessage("A zombie dropped a " + itemType + "; you have " + count + " " + itemType + plural + " now.");
    }

    // zombies info command
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

    // cheat spawn-zombie command
    @Override
    public void showCheatSpawnZombie(String zombieType, double x, double y) {
        displayMessage("Zombie of type " + zombieType
                + " is spawned at (" + String.format("%.1f", x) + ", " + (int) y + ")");
    }


    // plant selection errors
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

    // planting errors
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

    // plucking errors
    @Override
    public void errorNoPlantToPluck(int x, int y) {
        displayError("There is no plant to pluck at (" + x + ", " + y + ").");
    }

    @Override
    public void errorCannotPluckProtectedSeed(int x, int y) {
        displayError("Cannot pluck a protected seed at (" + x + ", " + y + ").");
    }

    // plant food errors
    @Override
    public void errorNoPlantFood() {
        displayError("You don't have any plant food left.");
    }

    @Override
    public void errorCannotFeedHere(int x, int y) {
        displayError("There is no plant to feed at (" + x + ", " + y + ").");
    }

    // boost errors
    @Override
    public void errorCannotBoostPlant(String type) {
        displayError("Cannot boost plant " + type + ".");
    }

    @Override
    public void errorNotEnoughDiamonds() {
        displayError("You don't have enough diamonds to boost this plant.");
    }

    // general errors
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