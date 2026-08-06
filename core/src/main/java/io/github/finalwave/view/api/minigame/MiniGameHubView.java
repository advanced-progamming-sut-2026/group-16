package io.github.finalwave.view.api.minigame;

import io.github.finalwave.model.minigame.MiniGameId;
import io.github.finalwave.view.api.View;

import java.util.List;

public interface MiniGameHubView extends View {

    void showCurrentMenu();

    void showGames(List<String> lines);

    void showStages(MiniGameId id, List<String> lines);

    void showEnteredGame(MiniGameId id);

    void showComingSoon(MiniGameId id);

    void errorInvalidCommand();

    void errorUnknownGame(String name);

    void errorGameLocked(String name);

    void errorNoGameSelected();

    void errorInvalidStage();

    void errorStageLocked(int stage);

    void errorStageNotFound(int stage);
}
