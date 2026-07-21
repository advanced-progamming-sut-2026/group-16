package model.game;

import model.adventure.LevelType;
import model.game.entity.plant.Plant;

public final class LoveYourPlantsHandler implements SpecialLevelHandler {

    public static final int DEFAULT_MAX_PLANTS_LOST = 5;

    private final int maxPlantsLost;

    public LoveYourPlantsHandler(int maxPlantsLost) {
        if (maxPlantsLost < 1) {
            throw new IllegalArgumentException("maxPlantsLost must be at least 1");
        }
        this.maxPlantsLost = maxPlantsLost;
    }

    public LoveYourPlantsHandler() {
        this(DEFAULT_MAX_PLANTS_LOST);
    }

    public int getMaxPlantsLost() {
        return maxPlantsLost;
    }

    @Override
    public LevelType getLevelType() {
        return LevelType.LOVE_YOUR_PLANTS;
    }

    @Override
    public void onLevelStart(GameSession session) {
        session.activateLoveYourPlants(maxPlantsLost);
    }

    @Override
    public void onPlantLost(GameSession session, Plant plant) {
        if (!session.isLoveYourPlantsActive() || session.getMatchResult() != MatchResult.IN_PROGRESS) {
            return;
        }
        if (session.getPlantsLost() < maxPlantsLost) {
            return;
        }
        MatchListener listener = session.getMatchListener();
        if (listener != null) {
            listener.onLoveYourPlantsLimitReached(session.getPlantsLost(), maxPlantsLost);
        }
        session.loseMatch();
    }
}
