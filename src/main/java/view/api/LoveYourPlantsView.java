package view.api;

public interface LoveYourPlantsView extends SpecialLevelView {

    void showLoveYourPlantsRule(int maxPlantsLost);

    void showPlantLossStatus(int plantsLost, int maxAllowed);

    void showLoveYourPlantsLimitReached(int plantsLost, int maxAllowed);
}
