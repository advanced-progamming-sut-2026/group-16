package view.api;

import model.game.SeedPlacement;

import java.util.List;

public interface SaveOurSeedsView extends SpecialLevelView {

    void showProtectedSeeds(List<SeedPlacement> seeds);

    void showDangerRows(List<Integer> rows);

    void showProtectedSeedDestroyed(String plantName, int x, int y);
}
