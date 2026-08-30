package io.github.finalwave.view.gui.render.sync;

import com.badlogic.gdx.math.Vector2;
import io.github.finalwave.model.game.entity.plant.ability.SquashAbility;
import io.github.finalwave.view.gui.render.LawnLayout;


public final class SquashArcMotion {
    private static final float ARC_HEIGHT_TILES = 1.65f;

    private SquashArcMotion() {
    }

    public static void position(LawnLayout layout,
                                int fromCol,
                                int fromRow,
                                int toCol,
                                int toRow,
                                SquashAbility.Phase phase,
                                float t,
                                Vector2 out) {
        Vector2 from = layout.cellCenter(fromCol, fromRow);
        Vector2 to = layout.cellCenter(toCol, toRow);
        float groundFrom = from.y;
        float groundTo = to.y;
        float peak = arcPeak(layout);
        float clamped = Math.max(0f, Math.min(1f, t));
        switch (phase) {
            case JUMP_UP_RIGHT, JUMP_UP_LEFT -> {
                out.x = lerp(from.x, to.x, clamped);
                out.y = groundFrom + peak * clamped;
            }
            case JUMP_DOWN_RIGHT, JUMP_DOWN_LEFT -> {
                out.x = to.x;
                out.y = groundTo + peak * (1f - clamped);
            }
            case TURN -> {
                out.x = from.x;
                out.y = groundFrom;
            }
            default -> out.set(from);
        }
    }

    private static float arcPeak(LawnLayout layout) {
        return layout.tileHeight() * ARC_HEIGHT_TILES;
    }

    private static float lerp(float from, float to, float t) {
        return from + (to - from) * t;
    }
}
