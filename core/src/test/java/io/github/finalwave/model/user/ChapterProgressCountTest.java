package io.github.finalwave.model.user;

import io.github.finalwave.model.adventure.ChapterId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChapterProgressCountTest {

    @Test
    void countCompletedLevelsSumsAcrossChapters() {
        ChapterProgress progress = new ChapterProgress();
        assertEquals(0, progress.countCompletedLevels());

        progress.markLevelCompleted(ChapterId.ANCIENT_EGYPT, 1);
        progress.markLevelCompleted(ChapterId.ANCIENT_EGYPT, 2);
        progress.markLevelCompleted(ChapterId.FROSTBITE_CAVES, 1);

        assertEquals(3, progress.countCompletedLevels());
    }
}
