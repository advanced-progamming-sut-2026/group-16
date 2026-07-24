package model.user;

import model.adventure.ChapterId;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChapterProgressUnlockResultTest {

    @Test
    void completingEgyptLevel1UnlocksFrostbiteOnce() {
        ChapterProgress progress = new ChapterProgress();

        ChapterProgress.LevelCompletionResult first =
                progress.markLevelCompleted(ChapterId.ANCIENT_EGYPT, 1);
        assertEquals(Optional.of(ChapterId.FROSTBITE_CAVES), first.newlyUnlockedChapter());
        assertFalse(first.completedFinalChapterGate());
        assertEquals(ChapterId.FROSTBITE_CAVES, progress.getUnlockedChapter());

        ChapterProgress.LevelCompletionResult replay =
                progress.markLevelCompleted(ChapterId.ANCIENT_EGYPT, 1);
        assertTrue(replay.newlyUnlockedChapter().isEmpty());
        assertFalse(replay.completedFinalChapterGate());
    }

    @Test
    void completingLastChapterLevel1ReportsFinalGate() {
        ChapterProgress progress = new ChapterProgress();
        progress.setUnlockedChapter(ChapterId.DARK_AGES);

        ChapterProgress.LevelCompletionResult result =
                progress.markLevelCompleted(ChapterId.DARK_AGES, 1);
        assertTrue(result.newlyUnlockedChapter().isEmpty());
        assertTrue(result.completedFinalChapterGate());
    }

    @Test
    void completingNonGateLevelDoesNotUnlock() {
        ChapterProgress progress = new ChapterProgress();
        ChapterProgress.LevelCompletionResult result =
                progress.markLevelCompleted(ChapterId.ANCIENT_EGYPT, 2);
        assertTrue(result.newlyUnlockedChapter().isEmpty());
        assertFalse(result.completedFinalChapterGate());
        assertEquals(ChapterId.ANCIENT_EGYPT, progress.getUnlockedChapter());
    }
}
