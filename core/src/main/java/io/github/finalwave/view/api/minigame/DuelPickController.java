package io.github.finalwave.view.api.minigame;

import java.util.List;

public interface DuelPickController {
    boolean zombieSide();

    List<String> pickPool();

    int pickSlots();

    List<String> localPicks();

    int pickSecondsLeft();

    void togglePick(String name);

    void submitPicks();
}
