package io.github.finalwave.view.gui.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.assets.LawnAssetIds;
import io.github.finalwave.view.gui.widget.PvzButtons;


public final class SunCounter extends Table {
    private static final float PLUS_SIZE = 56f;
    private static final float PLUS_GAP = 12f;
    private static final float ICON_SIZE = 72f;
    private static final float BANNER_HEIGHT = 48f;
    private static final float CHIP_WIDTH = 210f;
    private static final float CHIP_HEIGHT = 72f;
    private static final float BANNER_INSET = 36f;
    private static final float VALUE_PAD_LEFT = 96f;

    private final Label value;

    public SunCounter(GameAssets assets, Runnable onAdd) {
        SunChip chip = new SunChip(assets);
        value = chip.value();
        Actor plus = PvzButtons.iconButton(assets.region(LawnAssetIds.HUD_PLUS), PLUS_SIZE, PLUS_SIZE, onAdd);
        add(chip).size(CHIP_WIDTH, CHIP_HEIGHT).padRight(PLUS_GAP);
        add(plus).size(PLUS_SIZE);
    }

    public void setAmount(int amount) {
        value.setText(String.valueOf(Math.max(0, amount)));
    }

    private static final class SunChip extends WidgetGroup {
        private final Image banner;
        private final Image icon;
        private final Label value;

        private SunChip(GameAssets assets) {
            setTransform(false);
            banner = new Image(new NinePatchDrawable(new NinePatch(
                    assets.region(LawnAssetIds.SUN_BANNER), 16, 16, 8, 8)));
            banner.setScaling(Scaling.stretch);
            value = new Label("0", assets.skin(), outlineStyle(assets));
            value.setAlignment(Align.left);
            value.setColor(Color.WHITE);
            icon = new Image(new TextureRegionDrawable(assets.region(LawnAssetIds.SUN_ICON)));
            icon.setScaling(Scaling.fit);
            addActor(banner);
            addActor(value);
            addActor(icon);
            setSize(CHIP_WIDTH, CHIP_HEIGHT);
        }

        private Label value() {
            return value;
        }

        @Override
        public void layout() {
            float height = getHeight();
            float bannerY = (height - BANNER_HEIGHT) * 0.5f;
            banner.setBounds(BANNER_INSET, bannerY, getWidth() - BANNER_INSET, BANNER_HEIGHT);
            value.setBounds(VALUE_PAD_LEFT, bannerY, getWidth() - VALUE_PAD_LEFT - 12f, BANNER_HEIGHT);
            icon.setBounds(0f, (height - ICON_SIZE) * 0.5f, ICON_SIZE, ICON_SIZE);
            icon.toFront();
        }
    }

    private static String outlineStyle(GameAssets assets) {
        if (assets.skin().has("big_outline", Label.LabelStyle.class)) {
            return "big_outline";
        }
        if (assets.skin().has("medium_outline", Label.LabelStyle.class)) {
            return "medium_outline";
        }
        return "medium";
    }
}
