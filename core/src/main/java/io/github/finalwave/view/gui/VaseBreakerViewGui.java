package io.github.finalwave.view.gui;

import io.github.finalwave.controller.VaseBreakerController;
import io.github.finalwave.model.game.MatchResult;
import io.github.finalwave.model.game.entity.Vase;
import io.github.finalwave.model.game.entity.zombie.Zombie;
import io.github.finalwave.view.api.minigame.VaseBreakerView;
import io.github.finalwave.view.gui.screen.ScreenRouter;

import java.util.List;


public final class VaseBreakerViewGui extends GuiViewBase implements VaseBreakerView {
    public VaseBreakerViewGui(ScreenRouter router) {
        super(router);
    }

    public void bindController(VaseBreakerController controller) {
        if (controller != null) {
            controller.setDeferMatchExit(true);
        }
    }

    @Override
    public void showStageStarted(int stageIndex) {
        router.refreshGamePlayHud();
    }

    @Override
    public void showVaseSmashed(int col, int row, Vase.Content content) {
        router.refreshGamePlayHud();
    }

    @Override
    public void showSeedPacketDropped(String plantName, int col, int row) {
        router.refreshGamePlayHud();
    }

    @Override
    public void showSeedPacketExpired(String plantName, int col, int row) {
        router.refreshGamePlayHud();
    }

    @Override
    public void showSeedPacketPlanted(String plantName, int col, int row) {
        router.refreshGamePlayHud();
    }

    @Override
    public void showZombieSpawned(String type, double x, int row) {
    }

    @Override
    public void showZombieDied(String type, double x, double y) {
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
    public void showNukeActivated() {
        router.toastGamePlayMessage("Nuke activated.");
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
        router.toastGamePlayError("Invalid vasebreaker command.");
    }

    @Override
    public void errorNoVaseAt(int col, int row) {
        router.toastGamePlayError("No vase at (" + col + ", " + row + ").");
    }

    @Override
    public void errorNoSeedPacketAt(int col, int row) {
        router.toastGamePlayError("No seed packet available.");
    }

    @Override
    public void errorCannotPlantHere(int col, int row) {
        router.toastGamePlayError("Cannot plant here.");
    }

    @Override
    public void errorInvalidLocation(int col, int row) {
        router.toastGamePlayError("Invalid location.");
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
