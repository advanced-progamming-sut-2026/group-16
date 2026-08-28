package io.github.finalwave.view.gui.assets;

import java.util.Map;


public final class PlantPacketIds {
    private static final Map<String, String> OVERRIDES = Map.ofEntries(
            Map.entry(PlantNameAliases.normalize("Mega Gatling Pea"), "IMAGE_UI_PACKETS_MEGAGATLING"),
            Map.entry(PlantNameAliases.normalize("Cherry Bomb"), "IMAGE_UI_PACKETS_CHERRY_BOMB"),
            Map.entry(PlantNameAliases.normalize("Goo Peashooter"), "IMAGE_UI_PACKETS_PEASHOOTER"),
            Map.entry(PlantNameAliases.normalize("Giant Wall-nut"), "IMAGE_UI_PACKETS_TALLNUT"),
            Map.entry(PlantNameAliases.normalize("Twin Sunflower"), "IMAGE_UI_PACKETS_TWINSUNFLOWER")
    );

    private PlantPacketIds() {
    }

    public static String imageId(String plantName) {
        if (plantName == null || plantName.isBlank()) {
            return LawnAssetIds.PACKET_EMPTY;
        }
        String key = PlantNameAliases.normalize(plantName);
        String override = OVERRIDES.get(key);
        if (override != null) {
            return override;
        }
        return "IMAGE_UI_PACKETS_" + PlantNameAliases.pamKey(plantName);
    }

    public static String normalize(String name) {
        return PlantNameAliases.normalize(name);
    }
}
