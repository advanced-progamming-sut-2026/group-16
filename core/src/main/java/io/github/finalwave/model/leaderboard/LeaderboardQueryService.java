package io.github.finalwave.model.leaderboard;

import io.github.finalwave.model.quest.QuestService;
import io.github.finalwave.model.user.User;
import io.github.finalwave.model.user.UserDatabase;

import java.util.List;

public final class LeaderboardQueryService {
    private LeaderboardQueryService() {
    }

    public static List<LeaderboardEntry> loadAll(UserDatabase database) {
        List<User> users = database.getAllUsers();
        for (User user : users) {
            if (user != null) {
                QuestService.createTrackerFor(user, null);
            }
        }
        return LeaderboardService.build(users);
    }
}
