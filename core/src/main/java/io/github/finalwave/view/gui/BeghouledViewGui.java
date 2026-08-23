package io.github.finalwave.view.gui;

import io.github.finalwave.controller.BeghouledController;
import io.github.finalwave.model.game.MatchResult;
import io.github.finalwave.model.game.entity.zombie.Zombie;
import io.github.finalwave.model.minigame.beghouled.BeghouledUpgradeRule;
import io.github.finalwave.view.api.minigame.BeghouledView;
import io.github.finalwave.view.gui.screen.ScreenRouter;

import java.util.List;


public final class BeghouledViewGui extends GuiViewBase implements BeghouledView {
    public BeghouledViewGui(ScreenRouter router) {
        super(router);
    }

    public void bindController(BeghouledController controller) {
        if (controller != null) {
            controller.setDeferMatchExit(true);
        }
    }

    @Override
    public void showStageStarted(int stageIndex, int matchTarget, List<String> plantPool) {
        router.refreshGamePlayHud();
    }

    @Override
    public void showUpgrades(List<BeghouledUpgradeRule> upgrades) {
        router.refreshGamePlayHud();
    }

    @Override
    public void showSwapAccepted(int matchesCleared, int sunAwarded) {
        router.refreshGamePlayHud();
    }

    @Override
    public void showBoardReset() {
        router.toastGamePlayMessage("No more valid moves. The board was reset.");
    }

    @Override
    public void showUpgradeApplied(String fromPlant, String toPlant, int plantsConverted, int sunSpent) {
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
        router.toastGamePlayError("Invalid Beghouled command.");
    }

    @Override
    public void errorSwapOutOfBounds() {
        router.toastGamePlayError("Swap locations are out of bounds.");
    }

    @Override
    public void errorSwapNotAdjacent() {
        router.toastGamePlayError("Plants must be adjacent to swap.");
    }

    @Override
    public void errorSwapNoMatch() {
        router.toastGamePlayError("That swap does not create a match.");
    }

    @Override
    public void errorSwapMissingPlant() {
        router.toastGamePlayError("Both cells must have plants to swap.");
    }

    @Override
    public void errorSwapCraterBlocked() {
        router.toastGamePlayError("Cannot swap onto a crater.");
    }

    @Override
    public void errorUpgradeUnknown(String plantName) {
        router.toastGamePlayError("No upgrade available for " + plantName + ".");
    }

    @Override
    public void errorUpgradeInsufficientSun(int cost, int balance) {
        router.toastGamePlayError("Not enough sun for upgrade (cost=" + cost
                + ", balance=" + balance + ").");
    }

    @Override
    public void errorUpgradeNoPlants(String plantName) {
        router.toastGamePlayError("No " + plantName + " plants on the board to upgrade.");
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
