package io.github.finalwave.network.sync;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.finalwave.model.App;
import io.github.finalwave.model.user.User;
import io.github.finalwave.network.auth.LoginOkPayload;
import io.github.finalwave.profile.ProfileApplier;

import java.time.LocalDate;

public final class SyncCorrections {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private SyncCorrections() {
    }

    public static void applyWallet(User user, UpdateWalletPayload payload) {
        if (user == null || payload == null) {
            return;
        }
        user.setCoins(payload.getCoins());
        user.setDiamonds(payload.getDiamonds());
        user.setPlantFood(payload.getPlantFood());
        user.setGamesPlayed(payload.getGamesPlayed());
        user.setDailyOfferPlant(payload.getDailyOfferPlant());
        if (payload.getDailyOfferDate() == null || payload.getDailyOfferDate().isBlank()) {
            user.setDailyOfferDate(null);
        } else {
            user.setDailyOfferDate(LocalDate.parse(payload.getDailyOfferDate()));
        }
        user.setDailyOfferPurchased(payload.isDailyOfferPurchased());
        if (payload.getQuestDay() == null || payload.getQuestDay().isBlank()) {
            user.setQuestDay(null);
        } else {
            user.setQuestDay(LocalDate.parse(payload.getQuestDay()));
        }
    }

    public static void applyScoreGame(User user, UpdateScoreGamePayload payload) {
        if (user == null || payload == null) {
            return;
        }
        user.setHasPlayed(payload.isHasPlayed());
        if (payload.isHasPlayed()) {
            user.setBestMeowPoint(payload.getBestMeowPoint());
        }
    }

    public static void applyFromLoginOk(LoginOkPayload payload) {
        User user = ProfileApplier.apply(payload);
        App.getInstance().setCurrentUser(user);
    }
}
