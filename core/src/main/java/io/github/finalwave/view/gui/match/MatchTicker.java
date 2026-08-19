package io.github.finalwave.view.gui.match;

import io.github.finalwave.model.game.GameSession;


public interface MatchTicker {
    void advance(int ticks);

    GameSession session();
}
