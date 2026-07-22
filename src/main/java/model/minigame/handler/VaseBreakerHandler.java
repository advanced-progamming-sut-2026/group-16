package model.minigame.handler;

import model.game.GameSession;
import model.game.MatchResult;
import model.game.entity.zombie.Zombie;
import model.minigame.MiniGameHandler;

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
