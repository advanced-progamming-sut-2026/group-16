package controller;

import model.minigame.MiniGameId;
import model.user.User;
import view.api.minigame.StubMiniGameView;

public class ZombotanyController extends ViewController {

    private final MiniGameHubController hubController;

    public ZombotanyController(User user, MiniGameHubController hubController) {
        this.hubController = hubController;
    }

    @Override
    public void displayMenu() {
        getStubView().showComingSoon(MiniGameId.ZOMBOTANY.getDisplayName());
    }

    @Override
    public void handleCommand(String input) {
        if ("menu exit".equalsIgnoreCase(input.trim())) {
            parser.switchController(hubController);
            return;
        }
        getStubView().errorInvalidCommand();
    }

    private StubMiniGameView getStubView() {
        return (StubMiniGameView) view;
    }
}
