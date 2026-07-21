package model.game;

import model.adventure.AdventureRegistry;
import model.adventure.ChapterConfig;
import model.adventure.ChapterId;
import model.definition.PlantRegistry;
import model.definition.ZombieRegistry;
import model.game.mode.AdventureMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MapRendererDeadLineTest {

    private GameSession session;

    @BeforeEach
    void setUp() throws IOException {
        PlantRegistry plantRegistry = new PlantRegistry();
        plantRegistry.loadFromJson("src/main/resources/plants.json");
        ZombieRegistry zombieRegistry = new ZombieRegistry();
        zombieRegistry.loadFromJson("src/main/resources/zombies.json");
        zombieRegistry.loadArmorFromJson("src/main/resources/ArmorTypeData.json");

        ChapterConfig chapter = AdventureRegistry.getInstance().getChapter(ChapterId.BIG_WAVE_BEACH);
        AdventureMode mode = new AdventureMode(
                chapter, chapter.getLevel(3), plantRegistry, zombieRegistry, 1, new Random(1));
        session = mode.createSession();
        session.activateDeadLine(DeadLineHandler.DEFAULT_DEAD_LINE_COLUMN);
    }

    @Test
    void renderShowsDeadLineHeaderAndColumnMarker() {
        String map = MapRenderer.render(session);

        assertTrue(map.contains("Dead line: column 3"), map);
        assertTrue(map.contains("[DL]"), map);
    }
}
