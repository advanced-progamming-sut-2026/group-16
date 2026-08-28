package io.github.finalwave.view.gui.assets;

import java.util.Map;


public final class PlantNameAliases {
    private static final Map<String, String> PAM_KEYS = Map.of(
            normalize("Twin Sunflower"), "SUNFLOWERTWIN",
            normalize("Rotobaga"), "ROTORUTABAGA",
            normalize("Mega Gatling Pea"), "MEGAGATLING",
            normalize("Phat Beet"), "PHATBEETS",
            normalize("Giant Wall-nut"), "TALLNUT"
    );

    private PlantNameAliases() {
    }

    public static String pamKey(String plantName) {
        if (plantName == null || plantName.isBlank()) {
            return "";
        }
        String key = normalize(plantName);
        return PAM_KEYS.getOrDefault(key, key);
    }

    public static String normalize(String name) {
        return name.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
    }
}
