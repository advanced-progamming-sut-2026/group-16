package view.api;

public interface GameView extends View {
    void showGameMenu();

    void showCurrentMenu();

    void showCoinWallet(int coins);

    void showGemWallet(int diamonds);

    void showCheatAdded(String type, int amount);

    void errorInvalidCommand();

    void errorNotImplemented(String feature);

    void errorUnknownChapter(String chapterName);

    void errorChapterLocked(String chapterName);

    void errorLeaderboardNotImplemented();

    void errorInvalidCheatAmount();

    void errorInvalidCheatType();
}
