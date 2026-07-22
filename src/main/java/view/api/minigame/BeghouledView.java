package view.api.minigame;

import model.game.entity.zombie.Zombie;
import model.minigame.beghouled.BeghouledUpgradeRule;
import view.api.View;

import java.util.List;

public interface BeghouledView extends View {

    void showStageStarted(int stageIndex, int matchTarget, List<String> plantPool);

    void showUpgrades(List<BeghouledUpgradeRule> upgrades);

    void showSwapAccepted(int matchesCleared, int sunAwarded);

    void showBoardReset();

    void showUpgradeApplied(String fromPlant, String toPlant, int plantsConverted, int sunSpent);

    void showAdvanceTime(int ticks);

    void showMap(String mapRepresentation);

    void showZombiesInfo(List<Zombie> zombies);

    void showWinMessage();

    void showLoseMessage();

    void showCurrentMenu();

    void errorInvalidCommand();

    void errorSwapOutOfBounds();

    void errorSwapNotAdjacent();

    void errorSwapNoMatch();

    void errorSwapMissingPlant();

    void errorSwapCraterBlocked();

    void errorUpgradeUnknown(String plantName);

    void errorUpgradeInsufficientSun(int cost, int balance);

    void errorUpgradeNoPlants(String plantName);

    void errorInvalidTickCount();

    void errorNegativeTickCount();
}
