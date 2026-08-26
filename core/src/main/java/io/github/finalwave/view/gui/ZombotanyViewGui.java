package io.github.finalwave.view.gui;

import io.github.finalwave.controller.ZombotanyController;
import io.github.finalwave.model.game.MatchResult;
import io.github.finalwave.model.game.entity.zombie.Zombie;
import io.github.finalwave.view.api.minigame.ZombotanyView;
import io.github.finalwave.view.gui.screen.ScreenRouter;

import java.util.List;


public final class ZombotanyViewGui extends GuiViewBase implements ZombotanyView {
    public ZombotanyViewGui(ScreenRouter router) {
        super(router);
    }

    public void bindController(ZombotanyController controller) {
        if (controller != null) {
            controller.setDeferMatchExit(true);
        }
    }

    @Override
    public void showStageStarted(int stageIndex, int startingSun, List<String> plantPool) {
        router.refreshGamePlayHud();
    }

    @Override
    public void showSunAmount(int amount) {
        router.refreshGamePlayHud();
    }

    @Override
    public void showPlantPlanted(String plantType, int col, int row) {
        router.refreshGamePlayHud();
    }

    @Override
    public void showPlantPlucked(int col, int row) {
        router.refreshGamePlayHud();
    }

    @Override
    public void showAdvanceTime(int ticks) {
    }

    @Override
    public void showMap(String mapRepresentation) {
    }

    @Override
    public void showZombiesInfo(List<Zombie> zombies) {
    }

    @Override
    public void showWinMessage() {
        router.showGamePlayResult(MatchResult.WON);
    }

    @Override
    public void showLoseMessage() {
        router.showGamePlayResult(MatchResult.LOST);
    }

    @Override
    public void showCurrentMenu() {
    }

    @Override
    public void errorInvalidCommand() {
        router.toastGamePlayError("Invalid Zombotany command.");
    }

    @Override
    public void errorInvalidLocation(int col, int row) {
        router.toastGamePlayError("Invalid location: (" + col + ", " + row + ").");
    }

    @Override
    public void errorPlantNotFound(String plantType) {
        router.toastGamePlayError("Unknown plant: " + plantType + ".");
    }

    @Override
    public void errorPlantNotSelected(String plantType) {
        router.toastGamePlayError(plantType + " is not in this stage's loadout.");
    }

    @Override
    public void errorPlantOnCooldown(String plantType) {
        router.toastGamePlayError(plantType + " is still recharging.");
    }

    @Override
    public void errorNotEnoughSun() {
        router.toastGamePlayError("Not enough sun.");
    }

    @Override
    public void errorCannotPlantHere(int col, int row) {
        router.toastGamePlayError("Cannot plant at (" + col + ", " + row + ").");
    }

    @Override
    public void errorNoPlantToPluck(int col, int row) {
        router.toastGamePlayError("No plant to shovel at (" + col + ", " + row + ").");
    }

    @Override
    public void errorNoSunAt(int col, int row) {
        router.toastGamePlayError("No sun at (" + col + ", " + row + ").");
    }

    @Override
    public void errorInvalidTickCount() {
        router.toastGamePlayError("Invalid tick count.");
    }

    @Override
    public void errorNegativeTickCount() {
        router.toastGamePlayError("Tick count cannot be negative.");
    }
}
