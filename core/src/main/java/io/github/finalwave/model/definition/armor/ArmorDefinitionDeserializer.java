package io.github.finalwave.model.definition.armor;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.*;

public class ArmorDefinitionDeserializer extends JsonDeserializer<ArmorDefinition> {

    private static final Set<String> KNOWN_KEYS = new HashSet<>(List.of(
            "ArmorType", "BaseHealth", "ArmorFlags", "ArmorLayers",
            "ArmorLayerHealth", "ImpactSoundEvent"
    ));

    @Override
    public ArmorDefinition deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        ObjectMapper mapper = (ObjectMapper) p.getCodec();
        JsonNode root = mapper.readTree(p);

        String alias = firstAlias(root.get("aliases"));

        JsonNode data = root.get("objdata");
        if (data == null || data.isNull()) {
            data = mapper.createObjectNode();
        }

        String armorType = textOrNull(data.get("ArmorType"));
        int baseHealth = intOrDefault(data, "BaseHealth", 0);
        List<String> armorFlags = convertListOrEmpty(mapper, data.get("ArmorFlags"), String.class);
        List<String> armorLayers = convertListOrEmpty(mapper, data.get("ArmorLayers"), String.class);
        List<Double> armorLayerHealth = convertListOrEmpty(mapper, data.get("ArmorLayerHealth"), Double.class);
        String impactSoundEvent = textOrNull(data.get("ImpactSoundEvent"));

        Map<String, Object> extraProps = new LinkedHashMap<>();
        var fields = data.fields();
        while (fields.hasNext()) {
            var entry = fields.next();
            String key = entry.getKey();
            if (KNOWN_KEYS.contains(key)) {
                continue;
            }
            extraProps.put(key, mapper.convertValue(entry.getValue(), Object.class));
        }

        return new ArmorDefinition(
                alias, armorType, baseHealth,
                armorFlags, armorLayers, armorLayerHealth,
                impactSoundEvent, extraProps
        );
    }

    private static String firstAlias(JsonNode aliasesNode) {
        if (aliasesNode != null && aliasesNode.isArray() && aliasesNode.size() > 0) {
            return aliasesNode.get(0).asText();
        }
        return null;
    }

    private static String textOrNull(JsonNode node) {
        return (node == null || node.isNull()) ? null : node.asText();
    }

    private static int intOrDefault(JsonNode parent, String field, int def) {
        JsonNode n = parent.get(field);
        return (n == null || n.isNull()) ? def : n.asInt(def);
    }

    private static <T> List<T> convertListOrEmpty(ObjectMapper mapper, JsonNode node, Class<T> elementType) {
        if (node == null || node.isNull() || !node.isArray()) {
            return List.of();
        }
        List<T> result = new ArrayList<>(node.size());
        for (JsonNode element : node) {
            result.add(mapper.convertValue(element, elementType));
        }
        return result;
    }
}
