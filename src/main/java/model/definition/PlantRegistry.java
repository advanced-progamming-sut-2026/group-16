package model.definition;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.definition.plant.PlantDefinition;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class PlantRegistry {
    private final Map<String, PlantDefinition> byName = new LinkedHashMap<>();

    private final Map<Integer, PlantDefinition> byId = new LinkedHashMap<>();

    private final ObjectMapper mapper = new ObjectMapper();

    public void loadFromJson(String filePath) throws IOException {
        List<PlantDefinition> loaded = mapper.readValue(
                new File(filePath),
                mapper.getTypeFactory().constructCollectionType(List.class, PlantDefinition.class)
        );
        register(loaded);
    }

    public void loadFromJson(InputStream input) throws IOException {
        List<PlantDefinition> loaded = mapper.readValue(
                input,
                mapper.getTypeFactory().constructCollectionType(List.class, PlantDefinition.class)
        );
        register(loaded);
    }

    private void register(List<PlantDefinition> loaded) {
        byName.clear();
        byId.clear();
        for (PlantDefinition def : loaded) {
            byName.put(def.getName(), def);
            byId.put(def.getId(), def);
        }
    }

    public PlantDefinition getDefinition(String plantName) {
        return byName.get(plantName);
    }

    public List<PlantDefinition> getAllDefinitions() {
        return List.copyOf(byName.values());
    }

    public PlantDefinition getDefinitionById(int id) {
        return byId.get(id);
    }

    public List<PlantDefinition> getByCategory(String category) {
        return byName.values().stream()
                .filter(d -> category.equals(d.getCategory()))
                .toList();
    }

    public List<PlantDefinition> getByTag(String tag) {
        return byName.values().stream()
                .filter(d -> d.hasTag(tag))
                .toList();
    }

    public Map<String, PlantDefinition> getDefinitionsView() {
        return Collections.unmodifiableMap(byName);
    }

    public int size() {
        return byName.size();
    }
}
