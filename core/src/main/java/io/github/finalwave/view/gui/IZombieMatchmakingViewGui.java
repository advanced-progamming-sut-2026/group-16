package io.github.finalwave.view.gui;

import io.github.finalwave.controller.IZombieMatchmakingController;
import io.github.finalwave.network.match.ListMatchUsersResponse;
import io.github.finalwave.network.match.UserStatus;
import io.github.finalwave.view.api.minigame.IZombieMatchmakingView;
import io.github.finalwave.view.gui.screen.ScreenRouter;

public final class IZombieMatchmakingViewGui extends GuiViewBase implements IZombieMatchmakingView {

    public IZombieMatchmakingViewGui(ScreenRouter router) {
        super(router);
    }

    public void bindController(IZombieMatchmakingController controller) {
    }

    @Override
    public void showOptions() {
    }

    @Override
    public void showSearching(boolean searching) {
        router.showIZombieMatchmakingSearching(searching);
    }

    @Override
    public void showInvite(String inviteId, String fromUsername) {
        router.showChallengeInvite(inviteId, fromUsername);
    }

    @Override
    public void hideInvite() {
        router.hideChallengeInvite();
    }

    @Override
    public void showError(String message) {
        router.toastMatchmakingError(message);
    }

    @Override
    public void showUserStatus(String username, UserStatus status) {
        router.toastMatchmakingMessage(username + ": " + status.name());
    }

    @Override
    public void showPlayerDirectory(ListMatchUsersResponse response) {
        router.showIZombieMatchmakingDirectory(response);
    }

    @Override
    public void selectUsername(String username) {
        router.selectIZombieMatchmakingUsername(username);
    }
}
