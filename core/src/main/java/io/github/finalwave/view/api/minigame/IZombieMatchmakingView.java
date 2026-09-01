package io.github.finalwave.view.api.minigame;

import io.github.finalwave.network.match.ListMatchUsersResponse;
import io.github.finalwave.network.match.UserStatus;

public interface IZombieMatchmakingView {
    void showOptions();

    void showSearching(boolean searching);

    void showInvite(String inviteId, String fromUsername);

    void hideInvite();

    void showError(String message);

    void showUserStatus(String username, UserStatus status);

    void showPlayerDirectory(ListMatchUsersResponse response);

    void selectUsername(String username);
}
