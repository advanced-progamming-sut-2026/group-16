package view.api;

public interface GreenhouseView extends View {
    void showCurrentMenu();

    void showGreenhouse(String display);

    void showPlantPlantedInPot(int x, int y, String plantType);

    void showPotCollected(int x, int y, String reward);

    void showPlantGrowthAccelerated(int x, int y, int diamondsSpent);

    void errorPotLocked(int x, int y);

    void errorPotAlreadyOccupied(int x, int y);

    void errorNoPotToPlant(int x, int y);

    void errorNoPlantToCollect(int x, int y);

    void errorPlantNotReady(int x, int y);

    void errorNotEnoughDiamondsForAccelerate();

    void errorCannotAccelerateReadyPlant(int x, int y);

    void errorInvalidPotLocation(int x, int y);

    void errorInvalidCommand();
}
