package io.github.finalwave.view.gui;

import io.github.finalwave.controller.WalnutBowlingController;
import io.github.finalwave.model.game.MatchResult;
import io.github.finalwave.model.game.entity.zombie.Zombie;
import io.github.finalwave.model.minigame.bowling.BowlingNutType;
import io.github.finalwave.view.api.minigame.WalnutBowlingView;
import io.github.finalwave.view.gui.screen.ScreenRouter;

import java.util.List;


public final class WalnutBowlingViewGui extends GuiViewBase implements WalnutBowlingView {
    public WalnutBowlingViewGui(ScreenRouter router) {
        super(router);
    }

    public void bindController(WalnutBowlingController controller) {
        if (controller != null) {
            controller.setDeferMatchExit(true);
        }
    }

    @Override
    public void showStageStarted(int stageIndex, int redLineColumn) {
        router.refreshGamePlayHud();
    }

    @Override
    public void showConveyorBelt(List<String> plantsOnBelt) {
        router.refreshGamePlayHud();
    }

    @Override
    public void showConveyorBeltPlantArrived(String plantName) {
        router.refreshGamePlayHud();
    }

    @Override
    public void showBowlingNutSpawned(String plantName, int col, int row) {
        router.playBowlingSpawnSfx();
        router.refreshGamePlayHud();
    }

    @Override
    public void showBowlingNutHit(BowlingNutType type, String zombieType, double x, double row) {
        router.playBowlingImpactSfx();
    }

    @Override
    public void showBowlingNutExploded(int col, int row) {
        router.playExplosionSfx();
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
    public void errorInvalidCommand() {
        router.toastGamePlayError("Invalid walnut bowling command.");
    }

    @Override
    public void errorInvalidLocation(int col, int row) {
        router.toastGamePlayError("Invalid location: (" + col + ", " + row + ").");
    }

    @Override
    public void errorCannotPlantHere(int col, int row) {
        router.toastGamePlayError("Cannot plant here.");
    }

    @Override
    public void errorBeyondPlantingLine(int col, int row, int redLineColumn) {
        router.toastGamePlayError("Plant nuts left of the red line (column " + redLineColumn + ").");
    }

    @Override
    public void errorPlantNotOnConveyorBelt(String type) {
        router.toastGamePlayError("That nut is not on the conveyor yet.");
    }

    @Override
    public void errorUnknownPlant(String type) {
        router.toastGamePlayError("Unknown nut type.");
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
