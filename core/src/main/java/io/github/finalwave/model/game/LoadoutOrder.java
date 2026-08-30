package io.github.finalwave.model.game;

import io.github.finalwave.model.definition.plant.PlantDefinition;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public final class LoadoutOrder {

    private LoadoutOrder() {
    }

    public static List<String> effective(GameSession session) {
        if (session == null) {
            return List.of();
        }
        Set<String> selected = session.getSelectedLoadout();
        List<String> declared = session.getSelectedLoadoutOrder();
        if (declared != null && !declared.isEmpty()) {
            List<String> ordered = new ArrayList<>();
            for (String name : declared) {
                if (name != null && !name.isBlank() && (selected.isEmpty() || selected.contains(name))) {
                    ordered.add(name);
                }
            }
            if (!ordered.isEmpty()) {
                return List.copyOf(ordered);
            }
        }
        if (selected == null || selected.isEmpty()) {
            return List.of();
        }
        List<PlantDefinition> definitions = new ArrayList<>();
        for (PlantDefinition definition : session.getPlantRegistry().getAllDefinitions()) {
            if (selected.contains(definition.getName())) {
                definitions.add(definition);
            }
        }
        definitions.sort(Comparator.comparingInt(PlantDefinition::getId));
        List<String> names = new ArrayList<>(definitions.size());
        for (PlantDefinition definition : definitions) {
            names.add(definition.getName());
        }
        return List.copyOf(names);
    }
}
