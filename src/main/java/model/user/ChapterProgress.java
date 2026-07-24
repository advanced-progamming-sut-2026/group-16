package model.user;

import model.adventure.ChapterId;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class ChapterProgress {

    private ChapterId unlockedChapter = ChapterId.ANCIENT_EGYPT;
    private final Map<ChapterId, Set<Integer>> completedLevels = new EnumMap<>(ChapterId.class);

    public ChapterId getUnlockedChapter() {
        return unlockedChapter;
    }

    public boolean isChapterUnlocked(ChapterId chapter) {
        if (chapter == null) {
            return false;
        }
        return chapter.ordinal() <= unlockedChapter.ordinal();
    }

    public boolean isLevelCompleted(ChapterId chapter, int levelIndex) {
        return completedLevels.getOrDefault(chapter, Set.of()).contains(levelIndex);
    }

    public Set<Integer> getCompletedLevels(ChapterId chapter) {
        return Collections.unmodifiableSet(
                completedLevels.getOrDefault(chapter, Set.of()));
    }

    public LevelCompletionResult markLevelCompleted(ChapterId chapter, int levelIndex) {
        if (chapter == null || levelIndex < 1) {
            return LevelCompletionResult.none();
        }
        completedLevels.computeIfAbsent(chapter, ignored -> new HashSet<>()).add(levelIndex);
        Optional<ChapterId> newlyUnlocked = unlockNextIfNeeded(chapter, levelIndex);
        boolean completedFinalChapterGate = levelIndex == 1
                && chapter.ordinal() == ChapterId.values().length - 1;
        return new LevelCompletionResult(newlyUnlocked, completedFinalChapterGate);
    }

    private Optional<ChapterId> unlockNextIfNeeded(ChapterId chapter, int levelIndex) {
        // Completing NORMAL level 1 unlocks next chapter while specials are unimplemented.
        if (levelIndex == 1 && chapter.ordinal() < ChapterId.values().length - 1) {
            ChapterId next = ChapterId.values()[chapter.ordinal() + 1];
            if (next.ordinal() > unlockedChapter.ordinal()) {
                unlockedChapter = next;
                return Optional.of(next);
            }
        }
        return Optional.empty();
    }

    public record LevelCompletionResult(Optional<ChapterId> newlyUnlockedChapter,
                                        boolean completedFinalChapterGate) {
        public static LevelCompletionResult none() {
            return new LevelCompletionResult(Optional.empty(), false);
        }
    }

    public void setUnlockedChapter(ChapterId chapter) {
        if (chapter != null) {
            unlockedChapter = chapter;
        }
    }

    public void restoreCompletedLevel(ChapterId chapter, int levelIndex) {
        if (chapter == null || levelIndex < 1) {
            return;
        }
        completedLevels.computeIfAbsent(chapter, ignored -> new HashSet<>()).add(levelIndex);
    }

    public Map<ChapterId, Set<Integer>> getAllCompletedLevels() {
        Map<ChapterId, Set<Integer>> copy = new EnumMap<>(ChapterId.class);
        for (var entry : completedLevels.entrySet()) {
            copy.put(entry.getKey(), new HashSet<>(entry.getValue()));
        }
        return copy;
    }

    public int countCompletedLevels() {
        int total = 0;
        for (Set<Integer> levels : completedLevels.values()) {
            total += levels.size();
        }
        return total;
    }

    public Optional<CompletedLevel> furthestCompleted() {
        ChapterId bestChapter = null;
        int bestLevel = -1;
        for (Map.Entry<ChapterId, Set<Integer>> entry : completedLevels.entrySet()) {
            for (int level : entry.getValue()) {
                if (bestChapter == null
                        || entry.getKey().ordinal() > bestChapter.ordinal()
                        || (entry.getKey() == bestChapter && level > bestLevel)) {
                    bestChapter = entry.getKey();
                    bestLevel = level;
                }
            }
        }
        if (bestChapter == null) {
            return Optional.empty();
        }
        return Optional.of(new CompletedLevel(bestChapter, bestLevel));
    }

    public record CompletedLevel(ChapterId chapter, int levelIndex) {
        public String displayLabel() {
            return "Level " + levelIndex + " Chapter " + (chapter.ordinal() + 1);
        }

        public int sortKey() {
            return chapter.ordinal() * 100 + levelIndex;
        }
    }
}
