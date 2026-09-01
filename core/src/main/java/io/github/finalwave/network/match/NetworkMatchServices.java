package io.github.finalwave.network.match;

import io.github.finalwave.network.NetworkManager;

public final class NetworkMatchServices {
    private static NetworkManager networkManager;
    private static MatchmakingService matchmakingService;
    private static MatchSyncService matchSyncService;
    private static UserStatusService userStatusService;
    private static MatchDirectoryService matchDirectoryService;

    private NetworkMatchServices() {
    }

    public static void install(
            NetworkManager networkManagerValue,
            MatchmakingService matchmakingServiceValue,
            MatchSyncService matchSyncServiceValue,
            UserStatusService userStatusServiceValue,
            MatchDirectoryService matchDirectoryServiceValue) {
        networkManager = networkManagerValue;
        matchmakingService = matchmakingServiceValue;
        matchSyncService = matchSyncServiceValue;
        userStatusService = userStatusServiceValue;
        matchDirectoryService = matchDirectoryServiceValue;
    }

    public static boolean isOnlineCapable() {
        return networkManager != null && networkManager.isConnected()
                && matchmakingService != null && matchSyncService != null;
    }

    public static MatchmakingService matchmaking() {
        return matchmakingService;
    }

    public static MatchSyncService matchSync() {
        return matchSyncService;
    }

    public static UserStatusService userStatus() {
        return userStatusService;
    }

    public static MatchDirectoryService directory() {
        return matchDirectoryService;
    }
}
