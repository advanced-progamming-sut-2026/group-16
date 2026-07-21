package model.adventure;

import model.definition.PlantRegistry;
import model.definition.ZombieRegistry;
import model.game.GameSession;
import model.game.board.tile.GraveTile;
import model.game.board.tile.LowBeachTile;
import model.game.mode.AdventureMode;
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
        for (int row = 0; row < session.getBoard().getRows(); row++) {
            for (int col = 0; col < session.getBoard().getCols(); col++) {
                if (session.getBoard().getTile(col, row) instanceof LowBeachTile) {
                    water++;
                }
            }
        }
        assertTrue(water > 0);
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
