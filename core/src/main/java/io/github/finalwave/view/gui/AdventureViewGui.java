package io.github.finalwave.view.gui;

import io.github.finalwave.controller.AdventureController;
import io.github.finalwave.model.adventure.ChapterConfig;
import io.github.finalwave.model.adventure.LevelConfig;
import io.github.finalwave.view.api.AdventureView;
import io.github.finalwave.view.gui.screen.ScreenRouter;

import java.util.List;

public final class AdventureViewGui extends GuiViewBase implements AdventureView {
    public AdventureViewGui(ScreenRouter router) {
        super(router);
    }

    public void bindController(AdventureController controller) {
    }

    @Override
    public void showAdventureMenu(ChapterConfig chapter) {
        router.refreshAdventureMap();
    }

    @Override
    public void showLevels(ChapterConfig chapter, List<LevelConfig> levels) {
        router.refreshAdventureMap();
    }

    @Override
    public void showProgress(String progressText) {
        toast(progressText);
    }

    @Override
    public void showCurrentMenu(String chapterName) {
        router.refreshAdventureMap();
    }

    @Override
    public void errorChapterLocked(String chapterName) {
        toastError(chapterName + " is locked.");
    }

    @Override
    public void errorLevelLocked(int level) {
        toastError("Level " + level + " is locked.");
    }

    @Override
    public void errorLevelNotFound(int level) {
        toastError("Level " + level + " does not exist.");
    }

    @Override
    public void errorSpecialNotImplemented(String levelType) {
        toastError("Special level " + levelType + " is not implemented yet.");
    }

    @Override
    public void errorBossNotImplemented() {
        toastError("Boss level is not implemented yet.");
    }

    @Override
    public void errorInvalidCommand() {
        toastError("Invalid adventure command.");
    }

    @Override
    public void errorInvalidLevelNumber() {
        toastError("Invalid level number.");
    }
}
