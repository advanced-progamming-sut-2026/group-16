package io.github.finalwave.controller;

import io.github.finalwave.model.minigame.MiniGameStageConfig;
import io.github.finalwave.model.user.User;
import io.github.finalwave.network.match.ChallengeFailReason;
import io.github.finalwave.network.match.ChallengeInvitePayload;
import io.github.finalwave.network.match.ListMatchUsersResponse;
import io.github.finalwave.network.match.MatchDirectoryService;
import io.github.finalwave.network.match.MatchStartPayload;
import io.github.finalwave.network.match.MatchSyncService;
import io.github.finalwave.network.match.MatchmakingService;
import io.github.finalwave.network.match.UserStatus;
import io.github.finalwave.network.match.UserStatusService;
import io.github.finalwave.view.api.minigame.IZombieMatchmakingView;

public final class IZombieMatchmakingController extends ViewController implements MatchmakingService.Listener {

    private final User user;
    private final MatchmakingService matchmakingService;
    private final UserStatusService userStatusService;
    private final MatchDirectoryService matchDirectoryService;
    private final MatchSyncService matchSyncService;
    private final MiniGameStageConfig stage;

    public IZombieMatchmakingController(
            User user,
            MatchmakingService matchmakingService,
            UserStatusService userStatusService,
            MatchDirectoryService matchDirectoryService,
            MatchSyncService matchSyncService,
            MiniGameStageConfig stage) {
        this.user = user;
        this.matchmakingService = matchmakingService;
        this.userStatusService = userStatusService;
        this.matchDirectoryService = matchDirectoryService;
        this.matchSyncService = matchSyncService;
        this.stage = stage;
    }

    @Override
    public void displayMenu() {
        matchmakingView().showOptions();
        refreshDirectory();
    }

    public void refreshDirectory() {
        matchDirectoryService.refresh(response -> matchmakingView().showPlayerDirectory(response));
    }

    public User getUser() {
        return user;
    }

    public void back() {
        matchmakingService.leaveQueue();
        navigator.pop();
    }

    public void challengeUser(String username) {
        matchmakingService.challengeUser(username);
    }

    public void selectPlayer(String username) {
        matchmakingView().selectUsername(username);
    }

    public void joinRandomQueue() {
        matchmakingService.joinRandomQueue();
    }

    public void leaveQueue() {
        matchmakingService.leaveQueue();
        matchmakingView().showSearching(false);
    }

    public void respondInvite(String inviteId, boolean accepted) {
        matchmakingService.respondToInvite(inviteId, accepted);
        matchmakingView().hideInvite();
    }

    public void checkUserStatus(String username) {
        userStatusService.check(username).ifPresentOrElse(
                status -> matchmakingView().showUserStatus(username, status),
                () -> matchmakingView().showUserStatus(username, UserStatus.NOT_FOUND));
    }

    @Override
    public void onInvite(ChallengeInvitePayload invite) {
        if (invite != null) {
            matchmakingView().showInvite(invite.getInviteId(), invite.getFromUsername());
        }
    }

    @Override
    public void onChallengeFail(ChallengeFailReason reason) {
        matchmakingView().showError(matchmakingErrorMessage(reason));
        refreshDirectory();
    }

    private static String matchmakingErrorMessage(ChallengeFailReason reason) {
        if (reason == null) {
            return "Matchmaking failed";
        }
        return switch (reason) {
            case NOT_LOGGED_IN -> "Not logged in to server. Log out and sign in again.";
            case USER_OFFLINE -> "That player is offline.";
            case USER_NOT_FOUND -> "User not found.";
            case SELF_CHALLENGE -> "You cannot challenge yourself.";
            case BUSY -> "Player is busy.";
        };
    }

    @Override
    public void onChallengeRejected(String inviteId) {
        matchmakingView().showError("Challenge rejected");
    }

    @Override
    public void onChallengeTimeout(String inviteId) {
        matchmakingView().showError("Challenge timed out");
    }

    @Override
    public void onMatchStart(MatchStartPayload start) {
        matchmakingView().showSearching(false);
        matchmakingView().hideInvite();
        refreshDirectory();
        MiniGameHubController.launchNetworkedIZombieMatch(
                navigator, user, start, matchSyncService, stage);
    }

    @Override
    public void onQueueWaitingChanged(boolean waiting) {
        matchmakingView().showSearching(waiting);
        if (!waiting) {
            refreshDirectory();
        }
    }

    private IZombieMatchmakingView matchmakingView() {
        return (IZombieMatchmakingView) getView();
    }
}
