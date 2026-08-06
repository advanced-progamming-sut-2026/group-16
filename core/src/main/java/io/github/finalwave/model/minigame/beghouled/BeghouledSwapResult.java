package io.github.finalwave.model.minigame.beghouled;

public record BeghouledSwapResult(BeghouledSwapOutcome outcome,
                                  int matchesCleared,
                                  int sunAwarded,
                                  boolean boardReset) {

    public static BeghouledSwapResult failure(BeghouledSwapOutcome outcome) {
        return new BeghouledSwapResult(outcome, 0, 0, false);
    }
}
