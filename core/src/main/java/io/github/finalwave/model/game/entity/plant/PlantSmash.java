package io.github.finalwave.model.game.entity.plant;

import io.github.finalwave.model.game.entity.GameContext;
import io.github.finalwave.model.game.entity.zombie.Zombie;


public final class PlantSmash {

    private PlantSmash() {
    }

    public static boolean isSmasher(Zombie zombie) {
        if (zombie == null) {
            return false;
        }
        String type = normalize(zombie.getType());
        return type.contains("gargantuar") || type.contains("allstar") || type.contains("rockpunch");
    }

    public static boolean absorbsSmashFrom(Plant plant, Zombie zombie) {
        if (plant != null && plant.isEndurian()) {
            return isGargantuar(zombie);
        }
        return isSmasher(zombie);
    }

    public static void apply(Zombie zombie, Plant plant, GameContext context) {
        if (plant == null || !plant.isAlive()) {
            return;
        }
        if (absorbsSmashFrom(plant, zombie) && plant.tryAbsorbSmash()) {
            return;
        }
        plant.takeDamage(Integer.MAX_VALUE);
        if (plant.isDead() && context != null) {
            context.onPlantDestroyed(plant);
        }
    }

    private static boolean isGargantuar(Zombie zombie) {
        if (zombie == null) {
            return false;
        }
        return normalize(zombie.getType()).contains("gargantuar");
    }

    private static String normalize(String type) {
        if (type == null) {
            return "";
        }
        StringBuilder letters = new StringBuilder(type.length());
        for (int i = 0; i < type.length(); i++) {
            char c = type.charAt(i);
            if (Character.isLetterOrDigit(c)) {
                letters.append(Character.toLowerCase(c));
            }
        }
        return letters.toString();
    }
}
