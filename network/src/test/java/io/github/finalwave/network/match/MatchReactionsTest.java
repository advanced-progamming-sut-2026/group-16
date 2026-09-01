package io.github.finalwave.network.match;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class MatchReactionsTest {

    @Test
    void exposesExpectedCatalogSizes() {
        assertEquals(4, MatchReactions.textCount());
        assertEquals(4, MatchReactions.emojiCount());
        assertEquals(3, MatchReactions.stickerCount());
        assertEquals(4, MatchReactions.messages().length);
        assertEquals(4, MatchReactions.faces().length);
        assertEquals(3, MatchReactions.stickers().length);
        assertEquals(3, MatchReactions.stickerLabels().length);
    }

    @Test
    void describeReturnsNonBlankForEveryKindAndIndex() {
        for (int i = 0; i < MatchReactions.textCount(); i++) {
            assertFalse(MatchReactions.describe(MatchReactions.TEXT, i).isBlank());
        }
        for (int i = 0; i < MatchReactions.emojiCount(); i++) {
            assertFalse(MatchReactions.describe(MatchReactions.EMOJI, i).isBlank());
        }
        for (int i = 0; i < MatchReactions.stickerCount(); i++) {
            assertFalse(MatchReactions.describe(MatchReactions.STICKER, i).isBlank());
        }
    }
}
