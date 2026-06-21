package model.definition.zombie;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.*;

public class ZombieDefinitionDeserializer extends JsonDeserializer<ZombieDefinition> {

    private static final Set<String> KNOWN_KEYS = new HashSet<>(List.of(
            "Hitpoints", "EatDPS", "Speed", "WavePointCost", "Weight", "Cost",
            "CanSpawnPlantFood", "GroundTrackName",
            "AttackRect", "HitRect", "ArtCenter", "ShadowOffset",
            "ScaledProps", "ZombieStats", "ZombieArmorProps"
    ));

    @Override
    public ZombieDefinition deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        ObjectMapper mapper = (ObjectMapper) p.getCodec();
        JsonNode root = mapper.readTree(p);

        // --- envelope fields ---
        String alias = firstAlias(root.get("aliases"));
        String objClass = textOrNull(root.get("objclass"));

        JsonNode data = root.get("objdata");
        if (data == null || data.isNull()) {
            data = mapper.createObjectNode();
        }

        int hitpoints = intOrDefault(data, "Hitpoints", 0);
        int eatDps = intOrDefault(data, "EatDPS", 0);
        double speed = doubleOrDefault(data, "Speed", 0.0);
        int wavePointCost = intOrDefault(data, "WavePointCost", 0);
        int weight = intOrDefault(data, "Weight", 0);
        int cost = intOrDefault(data, "Cost", 0);
        boolean canSpawnPlantFood = boolOrDefault(data, "CanSpawnPlantFood", false);
        String groundTrackName = textOrNull(data.get("GroundTrackName"));

        Rect attackRect = convertOrNull(mapper, data.get("AttackRect"), Rect.class);
        Rect hitRect = convertOrNull(mapper, data.get("HitRect"), Rect.class);
        Point2D artCenter = convertOrNull(mapper, data.get("ArtCenter"), Point2D.class);
        ShadowOffset shadowOffset = convertOrNull(mapper, data.get("ShadowOffset"), ShadowOffset.class);

        List<ScaledProp> scaledProps = convertListOrEmpty(mapper, data.get("ScaledProps"), ScaledProp.class);
        List<ZombieStat> zombieStats = convertListOrEmpty(mapper, data.get("ZombieStats"), ZombieStat.class);
        List<String> armorRtids = convertListOrEmpty(mapper, data.get("ZombieArmorProps"), String.class);

        Map<String, Object> extraProps = new LinkedHashMap<>();
        var fields = data.fields();
        while (fields.hasNext()) {
            var entry = fields.next();
            String key = entry.getKey();
            if (KNOWN_KEYS.contains(key) || key.trim().startsWith("#")) {
                continue;
            }
            extraProps.put(key, mapper.convertValue(entry.getValue(), Object.class));
        }
        return new ZombieDefinition(
                alias, objClass,
                hitpoints, eatDps, speed, wavePointCost, weight, cost,
                canSpawnPlantFood, groundTrackName,
                attackRect, hitRect, artCenter, shadowOffset,
                scaledProps, zombieStats, armorRtids,
                extraProps
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

    private static double doubleOrDefault(JsonNode parent, String field, double def) {
        JsonNode n = parent.get(field);
        return (n == null || n.isNull()) ? def : n.asDouble(def);
    }

    private static boolean boolOrDefault(JsonNode parent, String field, boolean def) {
        JsonNode n = parent.get(field);
        return (n == null || n.isNull()) ? def : n.asBoolean(def);
    }

    private static <T> T convertOrNull(ObjectMapper mapper, JsonNode node, Class<T> type) {
        if (node == null || node.isNull()) {
            return null;
        }
        return mapper.convertValue(node, type);
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
