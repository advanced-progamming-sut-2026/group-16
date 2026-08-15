package io.github.finalwave.view.gui;

import io.github.finalwave.controller.TravelLogController;
import io.github.finalwave.view.api.TravelLogView;
import io.github.finalwave.view.gui.screen.ScreenRouter;

import java.util.List;

public final class TravelLogViewGui extends GuiViewBase implements TravelLogView {
    public TravelLogViewGui(ScreenRouter router) {
        super(router);
    }

    public void bindController(TravelLogController controller) {
    }

    @Override
    public void showCurrentMenu() {
        router.refreshTravelLog();
    }

    @Override
    public void showTravelLogPage(String pageName, List<String> questLines) {
        router.refreshTravelLog();
    }

    @Override
    public void showProgressSummary(List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            toast("No travel log progress yet.");
        } else {
            toast(String.join("  ", lines));
        }
        router.refreshTravelLog();
    }

    @Override
    public void errorInvalidCommand() {
        toastError("Invalid travel log command.");
    }

    @Override
    public void errorPageNameRequired() {
        toastError("Page name is required.");
    }

    @Override
    public void errorUnknownPage(String pageName) {
        toastError("Unknown travel log page: " + pageName + ".");
    }
}
