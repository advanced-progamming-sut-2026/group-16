package io.github.finalwave.model.game.entity.projectile;

import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.entity.plant.ability.ChomperAbility;


public final class ChomperMuzzles {

    public static final float SPECIAL_SECONDS = 1.9667f;
    public static final float BITE_SECONDS = 0.7333f;
    public static final float BITE_END_SECONDS = 0.8f;
    public static final float SPECIAL_END_SECONDS = 3.1667f;
    public static final float SPECIAL_END_GROWL_START_SECONDS = 1.93f;
    public static final float SPECIAL_END_GROWL_END_SECONDS = 2.6f;
    public static final float GROWL_PUSH_TILES = 0.15f;
    public static final float PLANT_FOOD_ON_SECONDS = 1.0f;
    public static final float PLANT_FOOD_SECONDS = 0.9667f;
    public static final float PLANT_FOOD_OFF_SECONDS = 1.9667f;
    public static final float PLANT_FOOD_BURP_SECONDS = 1.0333f;
    public static final float PLANT_FOOD_BURP_END_SECONDS = 0.7f;
    public static final float BURP_PUSH_TILES = 1.5f;
    public static final double MOUTH_OFFSET = 0.35;
    public static final double MOUTH_EAT_RADIUS = 0.12;
    public static final int BITE_DAMAGE = 200;
    public static final int PLANT_FOOD_INEDIBLE_DAMAGE = 1000;
    public static final int SWALLOW_CAPACITY = 2;
    public static final int PLANT_FOOD_PULL_COUNT = 3;

    private ChomperMuzzles() {
    }

    public static int phaseTicks(ChomperAbility.Phase phase) {
        return switch (phase) {
            case BITE -> secondsToTicks(BITE_SECONDS);
            case BITE_END -> secondsToTicks(BITE_END_SECONDS);
            case SWALLOW -> secondsToTicks(SPECIAL_SECONDS);
            case CHEW_END -> secondsToTicks(SPECIAL_END_SECONDS);
            case PF_ON -> secondsToTicks(PLANT_FOOD_ON_SECONDS);
            case PF_PULL -> secondsToTicks(PLANT_FOOD_SECONDS);
            case PF_OFF -> secondsToTicks(PLANT_FOOD_OFF_SECONDS);
            case PF_BURP -> secondsToTicks(PLANT_FOOD_BURP_SECONDS);
            case PF_BURP_END -> secondsToTicks(PLANT_FOOD_BURP_END_SECONDS);
            default -> 0;
        };
    }

    public static int chewTicks(double digestSeconds) {
        return Math.max(1, (int) Math.round(digestSeconds * GameSession.TICKS_PER_SECOND));
    }

    public static double mouthX(int plantCol) {
        return plantCol + MOUTH_OFFSET;
    }

    private static int secondsToTicks(float seconds) {
        return Math.max(1, Math.round(seconds * GameSession.TICKS_PER_SECOND));
    }
}
