package io.github.finalwave.model.adventure;

import io.github.finalwave.model.definition.PlantRegistry;
import io.github.finalwave.model.definition.ZombieRegistry;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.board.tile.GraveTile;
import io.github.finalwave.model.game.board.tile.LowBeachTile;
import io.github.finalwave.model.game.board.tile.WaterTile;
import io.github.finalwave.model.game.mode.AdventureMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class ChapterRulesBoardTest {

    private PlantRegistry plantRegistry;
    private ZombieRegistry zombieRegistry;

    @BeforeEach
    void setUp() throws IOException {
        plantRegistry = new PlantRegistry();
        plantRegistry.loadFromJson("src/main/resources/plants.json");
        zombieRegistry = new ZombieRegistry();
        zombieRegistry.loadFromJson("src/main/resources/zombies.json");
        zombieRegistry.loadArmorFromJson("src/main/resources/ArmorTypeData.json");
    }

    @Test
    void egyptPlacesGraves() {
        ChapterConfig chapter = AdventureRegistry.getInstance().getChapter(ChapterId.ANCIENT_EGYPT);
        AdventureMode mode = new AdventureMode(
                chapter, chapter.getLevel(1), plantRegistry, zombieRegistry, 3, new Random(1));
        GameSession session = mode.createSession();
        int graves = 0;
        for (int row = 0; row < session.getBoard().getRows(); row++) {
            for (int col = 0; col < session.getBoard().getCols(); col++) {
                if (session.getBoard().getTile(col, row) instanceof GraveTile) {
                    graves++;
                }
            }
        }
        assertTrue(graves >= 1);
        assertTrue(session.getSkySunSystem().isEnabled());
    }

    @Test
    void darkAgesDisablesSkySun() {
        ChapterConfig chapter = AdventureRegistry.getInstance().getChapter(ChapterId.DARK_AGES);
        AdventureMode mode = new AdventureMode(
                chapter, chapter.getLevel(1), plantRegistry, zombieRegistry, 3, new Random(2));
        GameSession session = mode.createSession();
        assertFalse(session.getSkySunSystem().isEnabled());
    }

    @Test
    void beachHasWaterColumns() {
        ChapterConfig chapter = AdventureRegistry.getInstance().getChapter(ChapterId.BIG_WAVE_BEACH);
        AdventureMode mode = new AdventureMode(
                chapter, chapter.getLevel(1), plantRegistry, zombieRegistry, 3, new Random(3));
        GameSession session = mode.createSession();
        int water = 0;
        int lowBeach = 0;
        int plainWater = 0;
        for (int row = 0; row < session.getBoard().getRows(); row++) {
            for (int col = 0; col < session.getBoard().getCols(); col++) {
                var tile = session.getBoard().getTile(col, row);
                if (tile != null && tile.isWater()) {
                    water++;
                }
                if (tile instanceof LowBeachTile) {
                    lowBeach++;
                }
                if (tile instanceof WaterTile) {
                    plainWater++;
                }
            }
        }
        assertTrue(water > 0);
        assertTrue(lowBeach > 0, "some cells should be designated low beach");
        assertTrue(plainWater > 0, "not every water cell should be low beach");
    }

    @Test
    void frostbiteSetsChillImmunity() {
        ChapterConfig chapter = AdventureRegistry.getInstance().getChapter(ChapterId.FROSTBITE_CAVES);
        AdventureMode mode = new AdventureMode(
                chapter, chapter.getLevel(1), plantRegistry, zombieRegistry, 3, new Random(4));
        GameSession session = mode.createSession();
        assertTrue(session.areZombiesImmuneToChill());
    }
}
