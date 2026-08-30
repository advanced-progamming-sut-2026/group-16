package io.github.finalwave.view.api;

public interface SettingView extends View {
    void showChangedDifficulty(int difficulty);

    void showSettingsMenu(int difficulty);

    void showCurrentMenu(int difficulty);

    void showOnlineStatus(String username, String status);

    void errorInvalidCommand();

    void errorInvalidDifficultyFormat();

    void errorDifficultyOutOfRange();
}
