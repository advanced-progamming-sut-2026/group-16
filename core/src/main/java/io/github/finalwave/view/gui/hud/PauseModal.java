package io.github.finalwave.view.gui.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.assets.LawnAssetIds;
import io.github.finalwave.view.gui.audio.GameAudioSettings;
import io.github.finalwave.view.gui.widget.PvzButtons;
import io.github.finalwave.view.gui.widget.StoreChrome;
import io.github.finalwave.view.gui.widget.ThemedSlider;


public final class PauseModal {
    private static final float PANEL_WIDTH = 760f;
    private static final float PANEL_HEIGHT = 420f;
    private static final float TOPPER_WIDTH = 640f;
    private static final float TOPPER_HEIGHT = 126f;
    private static final float PEEK_WIDTH = 176f;
    private static final float PEEK_HEIGHT = 118f;
    private static final float HEAD_WIDTH = 148f;
    private static final float HEAD_HEIGHT = 123f;
    private static final float BUTTON_WIDTH = 230f;
    private static final float BUTTON_HEIGHT = 56f;
    private static final float SLIDER_HEIGHT = 44f;

    private Group root;
    private Texture dimTexture;

    public void show(Table modalLayer,
                     Viewport viewport,
                     GameAssets assets,
                     Runnable onResume,
                     Runnable onRestart,
                     Runnable onExit) {
        dismiss();
        Skin skin = assets.skin();
        float width = viewport.getWorldWidth();
        float height = viewport.getWorldHeight();

        root = new Group();
        root.setSize(width, height);
        root.setTouchable(Touchable.childrenOnly);

        Image dimmer = new Image(dimDrawable());
        dimmer.setFillParent(true);
        dimmer.setColor(1f, 1f, 1f, 0.58f);
        dimmer.setTouchable(Touchable.enabled);
        dimmer.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
        });
        root.addActor(dimmer);

        WidgetGroup stageCard = new WidgetGroup();
        stageCard.setTransform(false);

        Table panel = new Table();
        panel.setBackground(StoreChrome.panel());
        panel.pad(48f, 52f, 92f, 52f);
        panel.add(volumeRow(assets, skin, "Music", GameAudioSettings.musicVolume(), assets.audio()::setMusicVolume))
                .growX()
                .padBottom(26f)
                .row();
        panel.add(volumeRow(assets, skin, "Sound FX", GameAudioSettings.sfxVolume(), assets.audio()::setSfxVolume))
                .growX();
        panel.setSize(PANEL_WIDTH, PANEL_HEIGHT);
        panel.setPosition(0f, 0f);
        panel.validate();
        stageCard.addActor(panel);

        Table peek = new Table();
        peek.setClip(true);
        peek.setTransform(false);
        peek.setTouchable(Touchable.disabled);
        peek.setSize(PEEK_WIDTH, PEEK_HEIGHT);
        peek.setPosition((PANEL_WIDTH - PEEK_WIDTH) * 0.5f, PANEL_HEIGHT - 8f);
        Image sunflower = image(assets, LawnAssetIds.PAUSE_SUNFLOWER, Scaling.fit);
        sunflower.setSize(HEAD_WIDTH, HEAD_HEIGHT);
        sunflower.setPosition((PEEK_WIDTH - HEAD_WIDTH) * 0.5f, 18f);
        sunflower.setOrigin(Align.center);
        sunflower.addAction(Actions.forever(Actions.sequence(
                Actions.rotateTo(6f, 0.85f, Interpolation.sine),
                Actions.rotateTo(-6f, 0.85f, Interpolation.sine)
        )));
        peek.addActor(sunflower);
        stageCard.addActor(peek);

        Image topper = image(assets, LawnAssetIds.PAUSE_TOPPER, Scaling.stretch);
        topper.setSize(TOPPER_WIDTH, TOPPER_HEIGHT);
        topper.setPosition((PANEL_WIDTH - TOPPER_WIDTH) * 0.5f, PANEL_HEIGHT - 42f);
        stageCard.addActor(topper);

        Image cornerGear = image(assets, LawnAssetIds.PAUSE_SLIDER_BOLT, Scaling.fit);
        cornerGear.setSize(58f, 58f);
        cornerGear.setPosition(PANEL_WIDTH - 86f, PANEL_HEIGHT - 34f);
        stageCard.addActor(cornerGear);

        Table buttons = new Table();
        buttons.defaults().size(BUTTON_WIDTH, BUTTON_HEIGHT).padLeft(8f).padRight(8f);
        buttons.add(skinButton(skin, "SAVE & EXIT", "brown", onExit));
        buttons.add(skinButton(skin, "RESTART", "brown", onRestart));
        buttons.add(skinButton(skin, "RESUME", "purple", onResume));
        buttons.setSize(PANEL_WIDTH, BUTTON_HEIGHT);
        buttons.setPosition(0f, -22f);
        buttons.validate();
        stageCard.addActor(buttons);

        float cardHeight = PANEL_HEIGHT + PEEK_HEIGHT;
        stageCard.setSize(PANEL_WIDTH, cardHeight);

        Label title = new Label("GAME PAUSED", skin, outlineStyle(skin, true));
        title.setAlignment(Align.center);
        title.setColor(Color.WHITE);
        title.setFontScale(0.92f);

        Table dialog = new Table();
        dialog.setFillParent(true);
        dialog.center();
        dialog.add(title).padBottom(6f).row();
        dialog.add(stageCard).size(PANEL_WIDTH, cardHeight).padTop(18f);
        root.addActor(dialog);
        modalLayer.addActor(root);
    }

    public void dismiss() {
        if (root != null) {
            root.remove();
            root = null;
        }
        if (dimTexture != null) {
            dimTexture.dispose();
            dimTexture = null;
        }
    }

    public boolean isShowing() {
        return root != null && root.getStage() != null;
    }

    private Table volumeRow(GameAssets assets, Skin skin, String title, int value, VolumeSink sink) {
        Label label = new Label(title, skin, outlineStyle(skin, false));
        label.setColor(Color.WHITE);
        label.setAlignment(Align.left);
        Slider slider = ThemedSlider.createVolume(skin, assets.region(LawnAssetIds.PAUSE_SLIDER_BOLT));
        slider.setValue(value);
        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                sink.accept(Math.round(slider.getValue()));
            }
        });
        Table row = new Table();
        row.add(label).width(186f).left().padRight(18f);
        row.add(slider).growX().height(SLIDER_HEIGHT).center();
        return row;
    }

    private TextButton skinButton(Skin skin, String text, String style, Runnable onClick) {
        return PvzButtons.textButton(text, skin, style, () -> {
            dismiss();
            if (onClick != null) {
                onClick.run();
            }
        });
    }

    private Drawable dimDrawable() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(0f, 0f, 0f, 1f);
        pixmap.fill();
        dimTexture = new Texture(pixmap);
        pixmap.dispose();
        return new TextureRegionDrawable(new TextureRegion(dimTexture));
    }

    private static Image image(GameAssets assets, String id, Scaling scaling) {
        Image image = new Image(new TextureRegionDrawable(assets.region(id)));
        image.setScaling(scaling);
        image.setTouchable(Touchable.disabled);
        return image;
    }

    private static String outlineStyle(Skin skin, boolean title) {
        if (title && skin.has("big_outline", Label.LabelStyle.class)) {
            return "big_outline";
        }
        if (skin.has("medium_outline", Label.LabelStyle.class)) {
            return "medium_outline";
        }
        return title ? "big" : "medium";
    }

    @FunctionalInterface
    private interface VolumeSink {
        void accept(int volume);
    }
}
