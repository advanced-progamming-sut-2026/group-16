package io.github.finalwave.view.cli;

import io.github.finalwave.model.game.SeedPlacement;
import io.github.finalwave.view.api.SaveOurSeedsView;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SaveOurSeedsLevelViewCli extends SpecialLevelViewCli implements SaveOurSeedsView {

    @Override
    public void showProtectedSeeds(List<SeedPlacement> seeds) {
        displayMessage("Save Our Seeds: protect the marked plants or you lose immediately.");
        if (seeds == null || seeds.isEmpty()) {
            displayMessage("Protected seeds: (none)");
            return;
        }
        List<String> parts = new ArrayList<>();
        for (SeedPlacement seed : seeds) {
            parts.add(seed.getPlantName() + " at (" + seed.getCol() + "," + seed.getRow() + ")");
        }
        displayMessage("Protected seeds: " + String.join(", ", parts));
    }

    @Override
    public void showDangerRows(List<Integer> rows) {
        if (rows == null || rows.isEmpty()) {
            displayMessage("Danger lines on rows: (none)");
            return;
        }
        String display = rows.stream()
                .map(row -> String.valueOf(row + 1))
                .collect(Collectors.joining(", "));
        displayMessage("Danger lines on rows: " + display);
    }

    @Override
    public void showProtectedSeedDestroyed(String plantName, int x, int y) {
        displayMessage("Protected seed " + plantName + " at (" + x + "," + y
                + ") was destroyed! You lose.");
    }
}
