package io.github.finalwave.profile;

import io.github.finalwave.model.user.User;
import io.github.finalwave.model.user.UserDatabase;

public final class LocalProfileCache {
    private LocalProfileCache() {
    }

    public static void sync(UserDatabase database, User user, String passwordHash) {
        if (database == null || user == null) {
            return;
        }
        database.replaceLocalProfileFromServer(user, passwordHash);
    }
}
