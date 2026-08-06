package io.github.finalwave.model.user;

import io.github.finalwave.model.minigame.MiniGameId;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class MiniGameProgress {

    private final Map<MiniGameId, Set<Integer>> completedStages = new EnumMap<>(MiniGameId.class);

    public boolean isStageCompleted(MiniGameId id, int stageIndex) {
        return completedStages.getOrDefault(id, Set.of()).contains(stageIndex);
    }

    public Set<Integer> getCompletedStages(MiniGameId id) {
        return Collections.unmodifiableSet(completedStages.getOrDefault(id, Set.of()));
    }

    public void markStageCompleted(MiniGameId id, int stageIndex) {
        if (id == null || stageIndex < 1) {
            return;
        }
        completedStages.computeIfAbsent(id, ignored -> new HashSet<>()).add(stageIndex);
    }

    public void restoreCompletedStage(MiniGameId id, int stageIndex) {
        markStageCompleted(id, stageIndex);
    }

    public int highestPlayableStage(MiniGameId id, int maxStage) {
        if (id == null || maxStage < 1) {
            return 1;
        }
        Set<Integer> completed = completedStages.getOrDefault(id, Set.of());
        int highest = 1;
        for (int stage = 1; stage <= maxStage; stage++) {
            if (completed.contains(stage)) {
                highest = Math.min(maxStage, stage + 1);
            } else {
                break;
            }
        }
        return highest;
    }

    public boolean isStagePlayable(MiniGameId id, int stageIndex, int maxStage) {
        return stageIndex >= 1 && stageIndex <= maxStage
                && stageIndex <= highestPlayableStage(id, maxStage);
    }

    public Map<MiniGameId, Set<Integer>> getAllCompletedStages() {
        Map<MiniGameId, Set<Integer>> copy = new EnumMap<>(MiniGameId.class);
        for (var entry : completedStages.entrySet()) {
            copy.put(entry.getKey(), new HashSet<>(entry.getValue()));
        }
        return copy;
    }

    public int completedStageCount() {
        int count = 0;
        for (Set<Integer> stages : completedStages.values()) {
            count += stages.size();
        }
        return count;
    }
}
