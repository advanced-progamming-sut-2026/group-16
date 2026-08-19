package io.github.finalwave.view.gui.input;

import io.github.finalwave.model.game.GameSession;


public interface LawnActionHost {
    GameSession session();

    void plantSeed(String plantName, int col, int row);

    boolean smashVase(int col, int row);

    boolean collectSunAt(int col, int row);

    boolean shovelAt(int col, int row);

    boolean feedAt(int col, int row);
}
