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

class MapRendererPlantWhatYouGetTest {

    private GameSession session;

    @BeforeEach
    void setUp() throws IOException {
        PlantRegistry plantRegistry = new PlantRegistry();
        plantRegistry.loadFromJson("src/main/resources/plants.json");
        ZombieRegistry zombieRegistry = new ZombieRegistry();
        zombieRegistry.loadFromJson("src/main/resources/zombies.json");
        zombieRegistry.loadArmorFromJson("src/main/resources/ArmorTypeData.json");

        ChapterConfig chapter = AdventureRegistry.getInstance().getChapter(ChapterId.DARK_AGES);
        AdventureMode mode = new AdventureMode(
                chapter, chapter.getLevel(3), plantRegistry, zombieRegistry, 1, new Random(1));
        session = mode.createSession();
        session.activatePlantWhatYouGet(PlantWhatYouGetHandler.DEFAULT_STARTING_SUN);
    }

    @Test
    void renderShowsPrepHintWhileInPrepPhase() {
        String map = MapRenderer.render(session);

        assertTrue(map.contains("Prep: plant freely, then: start zombie waves"), map);
    }
}
