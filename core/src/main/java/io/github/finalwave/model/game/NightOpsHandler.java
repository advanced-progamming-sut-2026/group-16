package io.github.finalwave.model.game;

import io.github.finalwave.model.adventure.LevelType;

public final class NightOpsHandler implements SpecialLevelHandler {
    @Override
    public LevelType getLevelType() {
        return LevelType.NIGHT_OPS;
    }

    @Override
    public void onLevelStart(GameSession session) {
        session.getSkySunSystem().setEnabled(false);
    }
}
