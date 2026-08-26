package io.github.finalwave.view.gui.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
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
    private static final float COUNT_PER_SECOND = 90f;
    private static final float CATCH_UP = 8f;

    private final SunChip chip;
    private final Label value;
    private float shown;
    private int actual;
    private int target;
    private int held;
    private boolean seeded;
    private boolean counting = true;
    private float countSpeed = 1f;

    public SunCounter(GameAssets assets, Runnable onAdd) {
        chip = new SunChip(assets);
        value = chip.value();
        Actor plus = PvzButtons.iconButton(assets.region(LawnAssetIds.HUD_PLUS), PLUS_SIZE, PLUS_SIZE, onAdd);
        add(chip).size(CHIP_WIDTH, CHIP_HEIGHT).padRight(PLUS_GAP);
        add(plus).size(PLUS_SIZE);
    }

    public void resetShown() {
        seeded = false;
        shown = 0;
        actual = 0;
        target = 0;
        held = 0;
        writeShown();
    }

    public void hold(int amount) {
        held += Math.max(0, amount);
        applyTarget();
    }

    public void release(int amount) {
        held = Math.max(0, held - Math.max(0, amount));
        applyTarget();
        pulse();
    }

    public void clearHeld() {
        held = 0;
        applyTarget();
    }

    public void setCounting(boolean counting) {
        this.counting = counting;
    }

    public void setCountSpeed(float countSpeed) {
        this.countSpeed = Math.max(0.25f, countSpeed);
    }

    public void setAmount(int amount) {
        actual = Math.max(0, amount);
        if (!seeded) {
            held = 0;
            shown = actual;
            target = actual;
            seeded = true;
            writeShown();
            return;
        }
        applyTarget();
    }

    private void applyTarget() {
        target = Math.max(0, actual - held);
        if (target < shown) {
            shown = target;
            writeShown();
        }
    }

    public Vector2 iconCenterStage(Vector2 out) {
        Vector2 point = out == null ? new Vector2() : out;
        Image icon = chip.icon();
        return icon.localToStageCoordinates(point.set(icon.getWidth() * 0.5f, icon.getHeight() * 0.5f));
    }

    public void pulse() {
        chip.clearActions();
        chip.setOrigin(Align.center);
        chip.setScale(1f);
        chip.addAction(Actions.sequence(
                Actions.scaleTo(1.16f, 1.16f, 0.08f, Interpolation.sineOut),
                Actions.scaleTo(1f, 1f, 0.14f, Interpolation.sine)));
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (!counting || shown >= target) {
            return;
        }
        float gap = target - shown;
        float step = Math.max(COUNT_PER_SECOND, gap * CATCH_UP) * delta * countSpeed;
        shown = Math.min(target, shown + step);
        writeShown();
    }

    private void writeShown() {
        value.setText(String.valueOf(Math.round(shown)));
    }

    private static final class SunChip extends WidgetGroup {
        private final Image banner;
        private final Image icon;
        private final Label value;

        private SunChip(GameAssets assets) {
            setTransform(true);
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

        private Image icon() {
            return icon;
        }

        @Override
        public void layout() {
            float height = getHeight();
            float bannerY = (height - BANNER_HEIGHT) * 0.5f;
            banner.setBounds(BANNER_INSET, bannerY, getWidth() - BANNER_INSET, BANNER_HEIGHT);
            value.setBounds(VALUE_PAD_LEFT, bannerY, getWidth() - VALUE_PAD_LEFT - 12f, BANNER_HEIGHT);
            icon.setBounds(0f, (height - ICON_SIZE) * 0.5f, ICON_SIZE, ICON_SIZE);
            icon.toFront();
            setOrigin(Align.center);
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
