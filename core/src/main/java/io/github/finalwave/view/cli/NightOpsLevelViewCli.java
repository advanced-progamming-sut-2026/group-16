package io.github.finalwave.view.cli;

import io.github.finalwave.view.api.NightOpsView;

public class NightOpsLevelViewCli extends SpecialLevelViewCli implements NightOpsView {

    @Override
    public void showNightOpsMode() {
        displayMessage("Night Ops: no sun from the sky - use sun-producing plants (e.g. Sunflower).");
    }
}
