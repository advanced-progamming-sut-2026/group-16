package io.github.finalwave.model.minigame.beghouled;

import io.github.finalwave.model.minigame.MiniGameStageConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BeghouledUpgradeCatalogTest {

    @Test
    void findsKnownRule() {
        BeghouledUpgradeCatalog catalog = BeghouledUpgradeCatalog.allRules();
        assertTrue(catalog.findRule("Peashooter").isPresent());
        assertEquals("Repeater", catalog.findRule("Peashooter").orElseThrow().toPlant());
        assertEquals(500, catalog.findRule("Peashooter").orElseThrow().sunCost());
    }

    @Test
    void missingLookupIsEmpty() {
        assertTrue(BeghouledUpgradeCatalog.allRules().findRule("Sunflower").isEmpty());
    }

    @Test
    void twoStageChainExists() {
        BeghouledUpgradeCatalog catalog = BeghouledUpgradeCatalog.allRules();
        assertEquals("Melon-pult", catalog.findRule("Cabbage-pult").orElseThrow().toPlant());
        assertEquals("Winter Melon", catalog.findRule("Melon-pult").orElseThrow().toPlant());
    }

    @Test
    void stageOneHasTwoUpgrades() {
        assertEquals(2, BeghouledUpgradeCatalog.stageOne().getRules().size());
        assertEquals(2, MiniGameStageConfig.beghouled(1).getUpgrades().size());
    }

    @Test
    void stageTwoAndThreeMatchCatalogs() {
        assertEquals(3, MiniGameStageConfig.beghouled(2).getUpgrades().size());
        assertEquals(6, MiniGameStageConfig.beghouled(3).getUpgrades().size());
        assertEquals(8, MiniGameStageConfig.beghouled(1).getMatchTarget());
        assertEquals(12, MiniGameStageConfig.beghouled(2).getMatchTarget());
        assertEquals(16, MiniGameStageConfig.beghouled(3).getMatchTarget());
    }
}
