package model.definition;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.definition.armor.ArmorDefinition;
import model.definition.zombie.ZombieDefinition;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * usage:
 * ZombieRegistry registry = new ZombieRegistry();
 * registry.loadFromJson("src/main/resources/zombies.json");
 * registry.loadArmorFromJson("src/main/resources/ArmorTypeData.json");
 */

public final class ZombieRegistry {

    private final Map<String, ZombieDefinition> definitions = new LinkedHashMap<>();
    private final Map<String, ArmorDefinition> armorDefinitions = new LinkedHashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();

    public void loadFromJson(String filePath) throws IOException {
        List<ZombieDefinition> loaded = mapper.readValue(
                new File(filePath),
                mapper.getTypeFactory().constructCollectionType(List.class, ZombieDefinition.class)
        );
        for (ZombieDefinition def : loaded) {
            definitions.put(def.getAlias(), def);
        }
    }

    public void loadArmorFromJson(String filePath) throws IOException {
        List<ArmorDefinition> loaded = mapper.readValue(
                new File(filePath),
                mapper.getTypeFactory().constructCollectionType(List.class, ArmorDefinition.class)
        );
        for (ArmorDefinition def : loaded) {
            armorDefinitions.put(def.getAlias(), def);
        }
    }

    public ZombieDefinition getDefinition(String alias) {
        return definitions.get(alias);
    }

    public List<ZombieDefinition> getAllDefinitions() {
        return List.copyOf(definitions.values());
    }

    public ArmorDefinition getArmorDefinition(String alias) {
        return armorDefinitions.get(alias);
    }

    public List<ArmorDefinition> getAllArmorDefinitions() {
        return List.copyOf(armorDefinitions.values());
    }

    /**
     * Convenience: resolves all armor pieces a zombie actually wears,
     * in one call, skipping any alias that wasn't found (e.g. if armor
     * data hasn't been loaded yet). Useful for runtime code (e.g.
     * ArmorBehavior) that just wants "give me the armor stat blocks
     * for this zombie" without manually looping over getArmorAliases().
     */
    public List<ArmorDefinition> resolveArmorFor(ZombieDefinition zombie) {
        List<ArmorDefinition> resolved = new java.util.ArrayList<>();
        for (String alias : zombie.getArmorAliases()) {
            ArmorDefinition armor = armorDefinitions.get(alias);
            if (armor != null) {
                resolved.add(armor);
            }
        }
        return resolved;
    }

    public Map<String, ZombieDefinition> getDefinitionsView() {
        return Collections.unmodifiableMap(definitions);
    }

    public Map<String, ArmorDefinition> getArmorDefinitionsView() {
        return Collections.unmodifiableMap(armorDefinitions);
    }
}
