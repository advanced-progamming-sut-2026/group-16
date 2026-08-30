package io.github.finalwave.model.game.entity.plant.support;

import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.LoadoutOrder;
import io.github.finalwave.model.game.entity.plant.Plant;

import java.util.List;
import java.util.Set;

public final class ImitaterMorphSupport {

    private static final Set<String> BLOCKED_TARGETS = Set.of("Imitater", "Cat-tail");

    private ImitaterMorphSupport() {
    }

    public static void onPlanted(Plant plant, GameSession session) {
        if (plant == null || session == null || !"Imitater".equals(plant.getName())) {
            return;
        }
        String target = resolveTarget(session);
        if (target == null) {
            return;
        }
        plant.setImitatedPlantName(target);
        plant.setImitaterMorphTicks(Math.max(8, GameSession.TICKS_PER_SECOND));
        plant.setAttacking(true);
    }

    public static String resolveTarget(GameSession session) {
        String selected = session.getImitaterTargetSeed();
        if (isValidTarget(session, selected)) {
            return selected;
        }
        List<String> order = LoadoutOrder.effective(session);
        int imitaterIndex = -1;
        for (int i = 0; i < order.size(); i++) {
            if ("Imitater".equals(order.get(i))) {
                imitaterIndex = i;
                break;
            }
        }
        if (imitaterIndex < 0) {
            return null;
        }
        for (int i = imitaterIndex - 1; i >= 0; i--) {
            String candidate = order.get(i);
            if (isValidTarget(session, candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private static boolean isValidTarget(GameSession session, String name) {
        if (name == null || name.isBlank() || BLOCKED_TARGETS.contains(name)) {
            return false;
        }
        return session.getPlantRegistry().getDefinition(name) != null;
    }
}
