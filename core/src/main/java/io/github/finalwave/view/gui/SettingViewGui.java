package io.github.finalwave.view.gui;

import io.github.finalwave.controller.SettingController;
import io.github.finalwave.view.api.SettingView;
import io.github.finalwave.view.gui.screen.ScreenRouter;

public final class SettingViewGui extends GuiViewBase implements SettingView {
    public SettingViewGui(ScreenRouter router) {
        super(router);
    }

    public void bindController(SettingController controller) {
    }

    @Override
    public void showChangedDifficulty(int difficulty) {
        router.refreshSettingsForm();
    }

    @Override
    public void showSettingsMenu(int difficulty) {
        router.refreshSettingsForm();
    }

    @Override
    public void showCurrentMenu(int difficulty) {
        toast("Current menu: settings");
        toast("Difficulty level: " + difficulty);
    }

    @Override
    public void errorInvalidCommand() {
        toastError("Invalid settings command.");
    }

    @Override
    public void errorInvalidDifficultyFormat() {
        toastError("Difficulty level must be a number between 1 and 5.");
    }

    @Override
    public void errorDifficultyOutOfRange() {
        toastError("Difficulty level must be between 1 and 5.");
    }
}
