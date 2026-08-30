package io.github.finalwave.server.db;

import io.github.finalwave.model.user.Gender;
import io.github.finalwave.model.user.User;

public final class DatabaseSanityCheck {
    public static final String SANITY_USERNAME = "__sanity_check__";

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

        User reloaded = database.getUser(SANITY_USERNAME);
        if (reloaded == null) {
            throw new IllegalStateException("registered user not found");
        }
        if (reloaded.getCoins() != 0 || reloaded.getDiamonds() != 0) {
            throw new IllegalStateException("wallet defaults incorrect");
        }
        if (reloaded.hasPlayed()) {
            throw new IllegalStateException("new user should not have played score game");
        }
        if (reloaded.getBestMeowPoint() != 0) {
            throw new IllegalStateException("new user should have no score");
        }

        reloaded.updateBestMeowPoint(42);
        database.saveBestMeowPoint(reloaded);

        User scored = database.getUser(SANITY_USERNAME);
        if (scored == null) {
            throw new IllegalStateException("scored user not found");
        }
        if (!scored.hasPlayed()) {
            throw new IllegalStateException("hasPlayed not persisted");
        }
        if (scored.getBestMeowPoint() != 42) {
            throw new IllegalStateException("bestMeowPoint not persisted");
        }
    }
}
