package io.github.finalwave.view.gui.assets;

import java.util.Map;


public final class PlantPacketIds {
    private static final Map<String, String> OVERRIDES = Map.ofEntries(
            Map.entry(normalize("Mega Gatling Pea"), "IMAGE_UI_PACKETS_MEGAGATLING"),
            Map.entry(normalize("Cherry Bomb"), "IMAGE_UI_PACKETS_CHERRY_BOMB"),
            Map.entry(normalize("Goo Peashooter"), "IMAGE_UI_PACKETS_PEASHOOTER"),
            Map.entry(normalize("Giant Wall-nut"), "IMAGE_UI_PACKETS_WALLNUT")
    );

    private PlantPacketIds() {
    }

    public static String imageId(String plantName) {
        if (plantName == null || plantName.isBlank()) {
            return LawnAssetIds.PACKET_EMPTY;
        }
        String key = normalize(plantName);
        String override = OVERRIDES.get(key);
        if (override != null) {
            return override;
        }
        return "IMAGE_UI_PACKETS_" + key;
    }

    public static String normalize(String name) {
        return name.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
    }
}
