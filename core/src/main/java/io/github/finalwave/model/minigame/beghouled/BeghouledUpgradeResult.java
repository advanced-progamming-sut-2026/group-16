package io.github.finalwave.model.minigame.beghouled;

public record BeghouledUpgradeResult(BeghouledUpgradeOutcome outcome,
                                     int plantsConverted,
                                     int sunSpent) {

    public static BeghouledUpgradeResult failure(BeghouledUpgradeOutcome outcome) {
        return new BeghouledUpgradeResult(outcome, 0, 0);
    }
}
