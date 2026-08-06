package io.github.finalwave.view.cli;

import io.github.finalwave.view.api.PlantWhatYouGetView;

public class PlantWhatYouGetLevelViewCli extends SpecialLevelViewCli implements PlantWhatYouGetView {

    @Override
    public void showPlantWhatYouGetRule(int startingSun) {
        displayMessage("Plant What You Get: start with " + startingSun
                + " sun. No sky sun. No sunflowers. Plant freely, then run: start zombie waves");
    }

    @Override
    public void showPrepPhaseHint() {
        displayMessage("Prep phase: plant without recharge. When ready, type: start zombie waves");
    }

    @Override
    public void showWavesStartedFromPrep() {
        displayMessage("Zombie waves started. Recharge and combat are now active.");
    }
}
