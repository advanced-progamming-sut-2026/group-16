package io.github.finalwave.view.cli;

import io.github.finalwave.view.api.LoveYourPlantsView;

public class LoveYourPlantsLevelViewCli extends SpecialLevelViewCli implements LoveYourPlantsView {

    @Override
    public void showLoveYourPlantsRule(int maxPlantsLost) {
        displayMessage("Love Your Plants: you lose if " + maxPlantsLost + " plants are destroyed.");
    }

    @Override
    public void showPlantLossStatus(int plantsLost, int maxAllowed) {
        displayMessage("Plants lost: " + plantsLost + "/" + maxAllowed + ".");
    }

    @Override
    public void showLoveYourPlantsLimitReached(int plantsLost, int maxAllowed) {
        displayError("Too many plants lost (" + plantsLost + "/" + maxAllowed + "). You lose!");
    }
}
