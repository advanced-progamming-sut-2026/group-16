package io.github.finalwave.view.gui.hud.special;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.assets.LawnAssetIds;

public final class LoveYourPlantsCounter extends Table {
    private static final float ICON_SIZE = 64f;
    private static final float BANNER_HEIGHT = 44f;
    private static final float CHIP_HEIGHT = 68f;
    private static final float CHIP_WIDTH = 196f;
    private static final float BANNER_INSET = 30f;
    private static final float TEXT_PAD_LEFT = 82f;
    private static final float TEXT_SCALE = 0.7f;

    private final Chip chip;
    private boolean shown;

    public LoveYourPlantsCounter(GameAssets assets) {
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

    public void refresh(GameSession session) {
        boolean active = session != null && session.isLoveYourPlantsActive();
        if (active != shown) {
            shown = active;
            setVisible(active);
            invalidateHierarchy();
        }
        if (!active) {
            return;
        }
        int remaining = Math.max(0, session.getLoveYourPlantsRemaining());
        int max = Math.max(1, session.getLoveYourPlantsMaxLoss());
        chip.refresh(remaining, max);
    }

    private static final class Chip extends WidgetGroup {
        private final Image banner;
        private final Image fill;
        private final Image icon;
        private final Label value;
        private float progress;

        private Chip(GameAssets assets) {
            setTransform(false);
            banner = new Image(new NinePatchDrawable(new NinePatch(
                    assets.region(LawnAssetIds.SUN_BANNER), 16, 16, 8, 8)));
            banner.setScaling(Scaling.stretch);
            fill = new Image(new TextureRegionDrawable(assets.region(LawnAssetIds.PROGRESS_FILL)));
            fill.setScaling(Scaling.stretch);
            icon = new Image(new TextureRegionDrawable(assets.region(LawnAssetIds.CHALLENGE_SUCCESS)));
            icon.setScaling(Scaling.fit);
            value = new Label("0 Left", assets.skin(), outlineStyle(assets));
            value.setAlignment(Align.left);
            value.setFontScale(TEXT_SCALE);
            value.setColor(Color.WHITE);
            addActor(banner);
            addActor(fill);
            addActor(value);
            addActor(icon);
            setSize(CHIP_WIDTH, CHIP_HEIGHT);
        }

        void refresh(int remaining, int max) {
            value.setText(remaining + " Left");
            if (remaining <= 1) {
                value.setColor(1f, 0.72f, 0.38f, 1f);
            } else {
                value.setColor(Color.WHITE);
            }
            progress = max <= 0 ? 0f : remaining / (float) max;
            layoutFill(progress);
        }

        @Override
        public void layout() {
            float height = getHeight();
            float bannerY = (height - BANNER_HEIGHT) * 0.5f;
            banner.setBounds(BANNER_INSET, bannerY, getWidth() - BANNER_INSET, BANNER_HEIGHT);
            value.setBounds(TEXT_PAD_LEFT, bannerY, getWidth() - TEXT_PAD_LEFT - 10f, BANNER_HEIGHT);
            icon.setBounds(0f, (height - ICON_SIZE) * 0.5f, ICON_SIZE, ICON_SIZE);
            layoutFill(progress);
            icon.toFront();
        }

        private void layoutFill(float progress) {
            float bannerY = (getHeight() - BANNER_HEIGHT) * 0.5f;
            float maxWidth = Math.max(1f, banner.getWidth()) * 0.92f;
            float fillWidth = maxWidth * Math.max(0f, Math.min(1f, progress));
            fill.setVisible(fillWidth > 1f);
            fill.setSize(fillWidth, BANNER_HEIGHT * 0.62f);
            fill.setPosition(banner.getX() + 6f, bannerY + (BANNER_HEIGHT - fill.getHeight()) * 0.5f);
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
