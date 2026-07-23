package view.api;

public interface ScoreGameView extends View {
    void showScoreGameMenu(int bestMeioPoint);

    void showCurrentMenu();

    void errorInvalidCommand();
}
