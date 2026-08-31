package io.github.finalwave.view.gui;

import io.github.finalwave.controller.IZombieController;
import io.github.finalwave.model.game.MatchResult;
import io.github.finalwave.model.game.entity.zombie.Zombie;
import io.github.finalwave.view.api.minigame.IZombieView;
import io.github.finalwave.view.gui.screen.ScreenRouter;

import java.util.List;
import java.util.Map;


public final class IZombieViewGui extends GuiViewBase implements IZombieView {
    public IZombieViewGui(ScreenRouter router) {
        super(router);
    }

    public void bindController(IZombieController controller) {
        if (controller != null) {
            controller.setDeferMatchExit(true);
        }
    }

    @Override
    public void showStageStarted(int stageIndex, int placementColumn, int startingSun) {
        router.refreshGamePlayHud();
    }

    @Override
    public void showRoster(List<String> names, Map<String, Integer> costs) {
        router.refreshGamePlayHud();
    }

    @Override
    public void showZombiePlaced(String name, int col, int row) {
        router.refreshGamePlayHud();
    }

    @Override
    public void showBrainEaten(int row) {
        router.toastGamePlayMessage("A zombie ate the brain on row " + (row + 1) + "!");
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
    public void showOpponentLeft() {
        router.dismissGamePlayResult();
        router.toastGamePlayMessage("Your opponent left the match.");
    }

    @Override
    public void errorInvalidCommand() {
        router.toastGamePlayError("Invalid I, Zombie command.");
    }

    @Override
    public void errorInvalidLocation(int col, int row) {
        router.toastGamePlayError("Invalid location: (" + col + ", " + row + ").");
    }

    @Override
    public void errorBeyondPlantingLine(int col, int row, int placementColumn) {
        router.toastGamePlayError("Place zombies right of the red line (column " + placementColumn + ").");
    }

    @Override
    public void errorNotInRoster(String type) {
        router.toastGamePlayError("That zombie is not in this stage's roster.");
    }

    @Override
    public void errorInsufficientSun(String type, int cost, int balance) {
        router.toastGamePlayError("Not enough sun (need " + cost + ", have " + balance + ").");
    }

    @Override
    public void errorUnknownZombie(String type) {
        router.toastGamePlayError("Unknown zombie type.");
    }

    @Override
    public void errorOnCooldown(String type) {
        router.toastGamePlayError("That zombie is still recharging.");
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
