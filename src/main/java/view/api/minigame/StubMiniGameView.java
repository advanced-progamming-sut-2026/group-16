package view.api.minigame;

import view.api.View;

public interface StubMiniGameView extends View {

    void showComingSoon(String miniGameName);

    void errorInvalidCommand();
}
