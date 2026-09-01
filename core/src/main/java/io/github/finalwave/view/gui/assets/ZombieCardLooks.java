package io.github.finalwave.view.gui.assets;

import io.github.finalwave.view.gui.render.clip.ArmorPartVisibility;
import pvz.libpvz.pam.PamPlayer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public final class ZombieCardLooks {
    private ZombieCardLooks() {
    }

    public static List<String> armorAliasesFor(String alias) {
        if (alias == null || alias.isBlank()) {
            return List.of();
        }
        return switch (alias) {
            case "ZombieArmor1" -> List.of("ConeDefault");
            case "ZombieArmor2" -> List.of("BucketDefault");
            case "ZombieArmor4" -> List.of("BrickDefault");
            case "ZombieDarkArmor3" -> List.of("ShoulderArmorDefault", "CrownDefault");
            case "ZombieNewspaper" -> List.of("NewspaperDefault");
            default -> List.of();
        };
    }

    public static Map<String, Boolean> intactArmorLeaves(
            PamPlayer player,
            EntityAnimationCatalog catalog,
            String alias,
            List<String> armorAliases) {
        if (player == null || catalog == null || alias == null || alias.isBlank()) {
            return null;
        }
        List<String> aliases = armorAliases == null || armorAliases.isEmpty()
                ? armorAliasesFor(alias)
                : armorAliases;
        if (aliases.isEmpty()) {
            return null;
        }
        String pamPath = catalog.zombiePath(alias);
        Map<String, Boolean> leaves = new HashMap<>();
        for (String armorAlias : aliases) {
            String[] layers = catalog.armorLayers(armorAlias);
            if (layers == null || layers.length == 0) {
                continue;
            }
            leaves.put(layers[0], Boolean.TRUE);
        }
        if (leaves.isEmpty()) {
            return null;
        }
        return ArmorPartVisibility.expand(player, pamPath, leaves);
    }
}
