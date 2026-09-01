package io.github.finalwave.view.gui.assets;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import io.github.finalwave.network.match.MatchReactions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StickerReactionCatalogTest {

    @Test
    void atlasCountMatchesReactionCatalog() {
        assertEquals(MatchReactions.stickerCount(), StickerReactionCatalog.count());
    }

    @Test
    void exposesExpectedAtlasBases() {
        assertEquals("STICKERS/chomper_sticker", StickerReactionCatalog.atlasBaseFor(0));
        assertEquals("STICKERS/jalapeno_sticker", StickerReactionCatalog.atlasBaseFor(1));
        assertEquals("STICKERS/bonkchoy_sticker", StickerReactionCatalog.atlasBaseFor(2));
    }

    @Test
    void atlasBasesUseFlatStickerLayout() {
        for (int i = 0; i < MatchReactions.stickerCount(); i++) {
            String base = StickerReactionCatalog.atlasBaseFor(i);
            assertNotNull(base);
            assertTrue(base.startsWith("STICKERS/"));
            assertTrue(base.endsWith("_sticker"));
        }
    }

    @Test
    void sortsChomperTilesInNumericOrder() {
        assertTrue(StickerReactionCatalog.tileOrder("tile000")
                < StickerReactionCatalog.tileOrder("tile010"));
        assertTrue(StickerReactionCatalog.tileOrder("tile002")
                < StickerReactionCatalog.tileOrder("tile010"));
    }

    @Test
    void sortsRowColumnFramesByRowThenColumn() {
        int[] early = StickerReactionCatalog.rowColOrder("row-1-column-2");
        int[] late = StickerReactionCatalog.rowColOrder("row-2-column-1");
        assertTrue(early[0] < late[0] || (early[0] == late[0] && early[1] < late[1]));
    }

    @Test
    void parsesBoundsAndKeepsChomperTileOrder() {
        String atlas = """
                chomper_sticker.png
                size: 939, 1565
                format: RGBA8888
                filter: Linear, Linear
                repeat: none
                pma: false
                tile010
                  bounds: 313, 626, 313, 313
                tile000
                  bounds: 0, 0, 313, 313
                """;
        List<StickerReactionCatalog.AtlasRect> rects = StickerReactionCatalog.parseAtlasBounds(atlas);
        assertEquals(2, rects.size());
        StickerReactionCatalog.sortRects(rects, 0);
        assertEquals("tile000", rects.get(0).name);
        assertEquals("tile010", rects.get(1).name);
        assertEquals(0, rects.get(0).x);
        assertEquals(0, rects.get(0).y);
        assertEquals(313, rects.get(0).width);
    }

    @Test
    void flipsAtlasYFromTopLeftToTextureCoordinates() {
        assertEquals(1252, StickerReactionCatalog.atlasYToTextureY(0, 1565, 313));
        assertEquals(0, StickerReactionCatalog.atlasYToTextureY(1252, 1565, 313));
    }

    @Test
    void sortsParsedRowColumnRectsByRowThenColumn() {
        String atlas = """
                jalapeno_sticker.png
                size: 1881, 941
                row-2-column-1
                  bounds: 1254, 0, 314, 313
                row-1-column-2
                  bounds: 314, 0, 313, 314
                """;
        List<StickerReactionCatalog.AtlasRect> rects =
                new ArrayList<>(StickerReactionCatalog.parseAtlasBounds(atlas));
        StickerReactionCatalog.sortRects(rects, 1);
        assertEquals("row-1-column-2", rects.get(0).name);
        assertEquals("row-2-column-1", rects.get(1).name);
    }
}
