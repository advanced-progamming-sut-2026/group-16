package io.github.finalwave.view.gui.render.clip;

import pvz.libpvz.pam.PamPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public final class ArmorPartVisibility {
    private static final Map<String, PartIndex> INDEX_BY_PATH = new HashMap<>();

    private ArmorPartVisibility() {
    }

    public static Map<String, Boolean> expand(PamPlayer player, String pamPath, Map<String, Boolean> enabledLeaves) {
        if (player == null || pamPath == null || enabledLeaves == null || enabledLeaves.isEmpty()) {
            return enabledLeaves;
        }
        PartIndex index = index(player, pamPath);
        Map<String, Boolean> expanded = new HashMap<>();
        for (Map.Entry<String, Boolean> entry : enabledLeaves.entrySet()) {
            if (!Boolean.TRUE.equals(entry.getValue()) || !index.names.contains(entry.getKey())) {
                continue;
            }
            markBranch(index, entry.getKey(), expanded);
        }
        return expanded.isEmpty() ? null : expanded;
    }

    public static boolean hasPart(PamPlayer player, String pamPath, String name) {
        if (player == null || pamPath == null || name == null) {
            return false;
        }
        return index(player, pamPath).names.contains(name);
    }

    public static void clear() {
        INDEX_BY_PATH.clear();
    }

    private static PartIndex index(PamPlayer player, String pamPath) {
        PartIndex cached = INDEX_BY_PATH.get(pamPath);
        if (cached != null) {
            return cached;
        }
        PartIndex built = new PartIndex();
        try {
            indexParts(player.getParts(pamPath), null, built);
        } catch (RuntimeException e) {
            return built;
        }
        if (!built.names.isEmpty()) {
            INDEX_BY_PATH.put(pamPath, built);
        }
        return built;
    }

    private static void markBranch(PartIndex index, String layer, Map<String, Boolean> vis) {
        vis.put(layer, Boolean.TRUE);
        String ancestor = index.parentOf.get(layer);
        while (ancestor != null && vis.put(ancestor, Boolean.TRUE) == null) {
            ancestor = index.parentOf.get(ancestor);
        }
        markDescendants(index, layer, vis);
    }

    private static void markDescendants(PartIndex index, String name, Map<String, Boolean> vis) {
        List<String> children = index.childrenOf.get(name);
        if (children == null) {
            return;
        }
        for (String child : children) {
            if (vis.put(child, Boolean.TRUE) == null) {
                markDescendants(index, child, vis);
            }
        }
    }

    private static void indexParts(PamPlayer.AnimationPart part, String parentName, PartIndex index) {
        if (part == null) {
            return;
        }
        String name = part.name;
        if (name != null) {
            index.names.add(name);
            if (parentName != null) {
                index.parentOf.put(name, parentName);
                index.childrenOf.computeIfAbsent(parentName, key -> new ArrayList<>()).add(name);
            }
        }
        String childParent = name != null ? name : parentName;
        for (PamPlayer.AnimationPart child : part.children) {
            indexParts(child, childParent, index);
        }
    }

    private static final class PartIndex {
        private final Set<String> names = new HashSet<>();
        private final Map<String, String> parentOf = new HashMap<>();
        private final Map<String, List<String>> childrenOf = new HashMap<>();
    }
}
