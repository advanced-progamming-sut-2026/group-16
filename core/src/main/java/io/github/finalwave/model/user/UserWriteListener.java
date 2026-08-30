package io.github.finalwave.model.user;

import io.github.finalwave.model.save.MatchSaveSnapshot;

import java.util.Set;

public interface UserWriteListener {
    default void onWalletChanged(User user) {
    }

    default void onPlantsChanged(User user, Set<String> plantNames) {
    }

    default void onGreenhousePotChanged(User user, GreenhousePot pot) {
    }

    default void onStoredBoostsChanged(User user) {
    }

    default void onUnlocked(User user, String kind, String name) {
    }

    default void onQuestProgressChanged(User user) {
    }

    default void onAdventureChanged(User user) {
    }

    default void onMiniGameStagesChanged(User user) {
    }

    default void onMatchSaved(User user, MatchSaveSnapshot snapshot) {
    }

    default void onMatchCleared(User user) {
    }

    default void onNewsChanged(User user) {
    }

    default void onSettingsChanged(User user) {
    }

    default void onScoreGameChanged(User user) {
    }
}
