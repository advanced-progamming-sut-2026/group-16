package io.github.finalwave.view.gui.widget;

import com.badlogic.gdx.scenes.scene2d.Touchable;
import io.github.finalwave.model.collection.CollectionPlantDetail;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.assets.PlantAnimationCatalog;


public final class UpgradeBadges {
    private static final float CLIP_SCALE = 0.42f;

    private UpgradeBadges() {
    }

    public static PamActor forPlant(GameAssets assets, CollectionPlantDetail plant) {
        PamActor badge = new PamActor(assets.pamPlayer());
        badge.setAnchor(0.5f, 0.5f);
        badge.setTouchable(Touchable.disabled);
        badge.setClip(PlantAnimationCatalog.UPGRADE_BADGE_PAM, clipFor(plant), CLIP_SCALE, true);
        return badge;
    }

    private static String clipFor(CollectionPlantDetail plant) {
        if (!plant.owned()) {
            return "locked";
        }
        if (plant.maxLevel() || plant.canUpgrade()) {
            return "idle";
        }
        return "no_charge";
    }
}
