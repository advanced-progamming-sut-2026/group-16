package io.github.finalwave.view.gui.hud.special;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.assets.LawnAssetIds;

public final class MeowPointBanner extends Table {
    private static final float BANNER_HEIGHT = 44f;
    private static final float CHIP_HEIGHT = 68f;
    private static final float CHIP_WIDTH = 210f;
    private static final float BANNER_INSET = 12f;
    private static final float TEXT_SCALE = 0.7f;

    private final Chip chip;
    private boolean shown;

    public MeowPointBanner(GameAssets assets) {
        chip = new Chip(assets);
        add(chip).size(CHIP_WIDTH, CHIP_HEIGHT);
        setVisible(false);
        shown = false;
        setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
    }

    @Override
    public float getPrefWidth() {
        return isVisible() ? CHIP_WIDTH : 0f;
    }

    @Override
    public float getPrefHeight() {
        return isVisible() ? CHIP_HEIGHT : 0f;
    }

    public void refresh(boolean active, int total) {
        if (active != shown) {
            shown = active;
            setVisible(active);
            invalidateHierarchy();
        }
        if (!active) {
            return;
        }
        chip.refresh(total);
    }

    private static final class Chip extends WidgetGroup {
        private final Image banner;
        private final Label value;

        private Chip(GameAssets assets) {
            setTransform(false);
            banner = new Image(new NinePatchDrawable(new NinePatch(
                    assets.region(LawnAssetIds.SUN_BANNER), 16, 16, 8, 8)));
            banner.setScaling(Scaling.stretch);
            value = new Label("0 Meow", assets.skin(), outlineStyle(assets));
            value.setAlignment(Align.center);
            value.setFontScale(TEXT_SCALE);
            value.setColor(Color.WHITE);
            addActor(banner);
            addActor(value);
            setSize(CHIP_WIDTH, CHIP_HEIGHT);
        }

        void refresh(int total) {
            value.setText(Math.max(0, total) + " Meow");
        }

        @Override
        public void layout() {
            float height = getHeight();
            float bannerY = (height - BANNER_HEIGHT) * 0.5f;
            banner.setBounds(BANNER_INSET, bannerY, getWidth() - BANNER_INSET * 2f, BANNER_HEIGHT);
            value.setBounds(banner.getX(), bannerY, banner.getWidth(), BANNER_HEIGHT);
        }
    }

    private static String outlineStyle(GameAssets assets) {
        if (assets.skin().has("medium_outline", Label.LabelStyle.class)) {
            return "medium_outline";
        }
        if (assets.skin().has("big_outline", Label.LabelStyle.class)) {
            return "big_outline";
        }
        return "medium";
    }
}
