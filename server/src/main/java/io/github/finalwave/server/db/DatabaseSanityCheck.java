package io.github.finalwave.server.db;

import io.github.finalwave.model.user.Gender;
import io.github.finalwave.model.user.User;

public final class DatabaseSanityCheck {
    public static final String SANITY_USERNAME = "__sanity_check__";
    private static final int SANITY_SCORE = 42;

    private DatabaseSanityCheck() {
    }

    public static void run(ServerDatabase database) {
        try {
            runOrThrow(database);
            System.out.println("Database sanity check passed");
        } catch (RuntimeException exception) {
            System.err.println("Database sanity check failed: " + exception.getMessage());
            System.exit(1);
        }
    }

    public static void runOrThrow(ServerDatabase database) {
        User existing = database.getUser(SANITY_USERNAME);
        if (existing == null) {
            registerSanityUser(database);
            verifyFreshUserDefaults(database);
        }
        verifyScoreRoundTrip(database);
    }

    private static void registerSanityUser(ServerDatabase database) {
        User user = new User(
                SANITY_USERNAME,
                "sanity-hash",
                "Sanity",
                "sanity@example.com",
                Gender.MALE
        );
        user.setSecurityQuestionId(1);
        user.setSecurityAnswerHash("sanity-answer-hash");
        database.registerUser(user);
    }

    private static void verifyFreshUserDefaults(ServerDatabase database) {
        User reloaded = requireSanityUser(database);
        if (reloaded.getCoins() != 0 || reloaded.getDiamonds() != 0) {
            throw new IllegalStateException("wallet defaults incorrect");
        }
        if (reloaded.hasPlayed()) {
            throw new IllegalStateException("new user should not have played score game");
        }
        if (reloaded.getBestMeowPoint() != 0) {
            throw new IllegalStateException("new user should have no score");
        }
    }

    private static void verifyScoreRoundTrip(ServerDatabase database) {
        User reloaded = requireSanityUser(database);
        reloaded.updateBestMeowPoint(SANITY_SCORE);
        database.saveBestMeowPoint(reloaded);

        User scored = requireSanityUser(database);
        if (!scored.hasPlayed()) {
            throw new IllegalStateException("hasPlayed not persisted");
        }
        if (scored.getBestMeowPoint() != SANITY_SCORE) {
            throw new IllegalStateException("bestMeowPoint not persisted");
        }
    }

    private static User requireSanityUser(ServerDatabase database) {
        User user = database.getUser(SANITY_USERNAME);
        if (user == null) {
            throw new IllegalStateException("registered user not found");
        }
        return user;
    }
}
