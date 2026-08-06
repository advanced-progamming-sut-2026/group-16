package io.github.finalwave.model.minigame.handler;

import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.MatchResult;
import io.github.finalwave.model.game.entity.zombie.Zombie;
import io.github.finalwave.model.minigame.MiniGameHandler;

public final class VaseBreakerHandler implements MiniGameHandler {

    @Override
    public void onTick(GameSession session) {
        if (session.getMatchResult() != MatchResult.IN_PROGRESS) {
            return;
        }
        if (!session.areAllVasesSmashed()) {
            return;
        }
        if (hasLivingZombies(session)) {
            return;
        }
        session.winMatch();
    }

    private static boolean hasLivingZombies(GameSession session) {
        for (Zombie zombie : session.getZombies()) {
            if (zombie.isAlive()) {
                return true;
            }
        }
        return false;
    }
}
