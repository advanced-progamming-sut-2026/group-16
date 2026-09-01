package io.github.finalwave.network.match;

import io.github.finalwave.controller.MiniGameHubController;
import io.github.finalwave.controller.Navigator;
import io.github.finalwave.model.minigame.MiniGameStageConfig;
import io.github.finalwave.model.user.User;

import java.util.function.Supplier;

public final class MatchLaunchBridge {

    private MatchLaunchBridge() {
    }

    public static void install(
            MatchmakingService matchmaking,
            MatchSyncService matchSync,
            Supplier<Navigator> navigator,
            Supplier<User> currentUser) {
        matchmaking.setMatchStartHandler(start -> {
            User user = currentUser.get();
            if (user == null || start == null || start.getMatchId() == null || start.getMatchId().isBlank()) {
                return;
            }
            String activeMatchId = matchSync.matchId();
            if (activeMatchId != null && activeMatchId.equals(start.getMatchId())) {
                return;
            }
            Navigator nav = navigator.get();
            if (nav == null) {
                return;
            }
            MiniGameStageConfig stage = MiniGameStageConfig.iZombieNetwork();
            MiniGameHubController.launchNetworkedIZombieMatch(nav, user, start, matchSync, stage);
            matchmaking.setListener(null);
        });
    }
}
