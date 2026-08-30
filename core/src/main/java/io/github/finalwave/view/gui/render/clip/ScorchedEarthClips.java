package io.github.finalwave.view.gui.render.clip;

import io.github.finalwave.view.gui.assets.EntityAnimationCatalog;


public final class ScorchedEarthClips {
    public static final String TILE_PATH =
            "768/FULL/EFFECTS/SCORCHED_EARTH_TILE/SCORCHED_EARTH_TILE.PAM";
    public static final String EDGE_PATH =
            "768/FULL/EFFECTS/SCORCHED_EARTH_EDGE/SCORCHED_EARTH_EDGE.PAM";

    private ScorchedEarthClips() {
    }

    public static EntityAnimationCatalog.ClipSpec tileAppear() {
        return new EntityAnimationCatalog.ClipSpec(TILE_PATH, "animation");
    }

    public static EntityAnimationCatalog.ClipSpec tileIdle() {
        return new EntityAnimationCatalog.ClipSpec(TILE_PATH, "animation2");
    }

    public static EntityAnimationCatalog.ClipSpec tileExit() {
        return new EntityAnimationCatalog.ClipSpec(TILE_PATH, "animation3");
    }

    public static EntityAnimationCatalog.ClipSpec edgeAppear() {
        return new EntityAnimationCatalog.ClipSpec(EDGE_PATH, "animation");
    }

    public static EntityAnimationCatalog.ClipSpec edgeIdle() {
        return new EntityAnimationCatalog.ClipSpec(EDGE_PATH, "animation2");
    }
}
