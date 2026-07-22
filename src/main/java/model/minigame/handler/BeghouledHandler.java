package model.minigame.handler;

import model.game.GameSession;
import model.game.MatchResult;
import model.game.entity.plant.Plant;
import model.minigame.MiniGameHandler;

public final class BeghouledHandler implements MiniGameHandler {

    @Override
    public void onTick(GameSession session) {
        if (session.getMatchResult() != MatchResult.IN_PROGRESS) {
            return;
        }
        if (!session.isBeghouledActive()) {
            return;
        }
        if (session.getBeghouledBoard().getMatchesMade() >= session.getBeghouledMatchTarget()) {
            session.nukeAllZombies();
            session.winMatch();
        }
    }

    @Override
    public void onPlantLost(GameSession session, Plant plant) {
        if (!session.isBeghouledActive() || plant == null) {
            return;
        }
        session.getBeghouledBoard().markCrater(session, plant.getCol(), plant.getRow());
    }
}
