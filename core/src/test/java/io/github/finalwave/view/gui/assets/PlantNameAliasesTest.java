package io.github.finalwave.view.gui.assets;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


class PlantNameAliasesTest {

    @Test
    void twinSunflowerMapsToSunflowerTwinPamKey() {
        assertEquals("SUNFLOWERTWIN", PlantNameAliases.pamKey("Twin Sunflower"));
    }

    @Test
    void rotobagaMapsToRotorutabagaPam() {
        assertEquals("ROTORUTABAGA", PlantNameAliases.pamKey("Rotobaga"));
        assertEquals("IMAGE_UI_PACKETS_ROTORUTABAGA", PlantPacketIds.imageId("Rotobaga"));
    }
}
