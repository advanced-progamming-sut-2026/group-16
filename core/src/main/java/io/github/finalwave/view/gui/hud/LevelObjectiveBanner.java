package io.github.finalwave.view.gui.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.finalwave.model.adventure.LevelConfig;
import io.github.finalwave.model.adventure.LevelType;
import io.github.finalwave.model.game.PlantWhatYouGetHandler;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.assets.LawnAssetIds;
import io.github.finalwave.view.gui.assets.MenuAssetIds;
import io.github.finalwave.view.gui.widget.PriceButton;

import java.util.ArrayList;
import java.util.List;


public final class LevelObjectiveBanner {
    private static final float PANEL_WIDTH = 980f;
    private static final float PANEL_HEIGHT = 430f;
    private static final float HEADER_HEIGHT = 80f;
    private static final float BUTTON_WIDTH = 300f;
    private static final float BUTTON_HEIGHT = 72f;
    private static final float BUTTON_OVERLAP = 26f;
    private static final float ORB_SIZE = 36f;
    private static final float ROW_GAP = 34f;
    private static final float TEXT_SCALE_BIG = 0.76f;
    private static final float TEXT_SCALE_MEDIUM = 1.18f;
    private static final Color TITLE_WHITE = Color.WHITE;
    private static final Color BODY_BROWN = new Color(0.22f, 0.16f, 0.10f, 1f);
    private static final String BORDER_SKIN = "image_ui_dialog_asset_dialogborder_10";
    private static final String INNER_SKIN = "image_ui_dialog_asset_inner_bkgd_10";

    private static Texture dimTexture;
    private static Texture headerTexture;
    private static Texture orbTexture;

    private Group root;
    private boolean shown;

    public void show(Stage stage,
                     Viewport viewport,
                     GameAssets assets,
                     LevelConfig level,
                     Runnable onContinue) {
        if (shown || level == null || stage == null) {
            if (onContinue != null && !isShowing()) {
                onContinue.run();
            }
            return;
        }
        shown = true;
        dismissRoot();
        Skin skin = assets.skin();
        float width = viewport.getWorldWidth();
        float height = viewport.getWorldHeight();

        root = new Group();
        root.setSize(width, height);
        root.setTouchable(Touchable.childrenOnly);

        Image dimmer = new Image(dimDrawable());
        dimmer.setFillParent(true);
        dimmer.setColor(0f, 0f, 0f, 0.62f);
        dimmer.setTouchable(Touchable.enabled);
        dimmer.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
        });
        root.addActor(dimmer);

        Table inner = new Table();
        inner.setBackground(innerPanel(skin, assets));
        inner.top();
        inner.add(header(skin)).growX().height(HEADER_HEIGHT).row();
        Table body = new Table();
        body.center();
        body.add(objectiveList(skin, level)).growX().left();
        inner.add(body).grow().pad(18f, 88f, 44f, 72f);

        Table frame = new Table();
        frame.setBackground(borderPanel(skin, assets));
        frame.pad(20f, 22f, 22f, 22f);
        frame.add(inner).grow();
        frame.setSize(PANEL_WIDTH, PANEL_HEIGHT);

        Actor continueButton = PriceButton.labeled(
                skin,
                "CONTINUE",
                BUTTON_WIDTH,
                BUTTON_HEIGHT,
                patch(assets, LawnAssetIds.PURPLE_BUTTON, 28, 28, 16, 16),
                patch(assets, LawnAssetIds.PURPLE_BUTTON_DOWN, 28, 28, 16, 16),
                () -> {
                    dismissRoot();
                    if (onContinue != null) {
                        onContinue.run();
                    }
                });

        WidgetGroup card = new WidgetGroup();
        card.setTransform(false);
        float cardHeight = PANEL_HEIGHT + BUTTON_OVERLAP;
        card.setSize(PANEL_WIDTH, cardHeight);
        frame.setPosition(0f, BUTTON_OVERLAP);
        continueButton.setPosition((PANEL_WIDTH - BUTTON_WIDTH) * 0.5f, 0f);
        card.addActor(frame);
        card.addActor(continueButton);

        Table dialog = new Table();
        dialog.setFillParent(true);
        dialog.center();
        dialog.add(card).size(PANEL_WIDTH, cardHeight);
        root.addActor(dialog);
        stage.addActor(root);
    }

    public void reset() {
        shown = false;
        dismissRoot();
    }

    public void dismiss() {
        dismissRoot();
    }

    public boolean isShowing() {
        return root != null && root.getStage() != null;
    }

    private void dismissRoot() {
        if (root != null) {
            root.remove();
            root = null;
        }
    }

    private Table header(Skin skin) {
        Label title = new Label("Level Objectives", skin, outlineStyle(skin));
        title.setAlignment(Align.center);
        title.setColor(TITLE_WHITE);
        title.setFontScale(0.88f);
        Table bar = new Table();
        bar.setBackground(headerDrawable());
        bar.add(title).expand().center();
        return bar;
    }

    private Table objectiveList(Skin skin, LevelConfig level) {
        Table list = new Table();
        list.left();
        String style = bodyStyle(skin);
        float scale = "big".equals(style) ? TEXT_SCALE_BIG : TEXT_SCALE_MEDIUM;
        List<String> lines = objectives(level);
        for (int i = 0; i < lines.size(); i++) {
            Image orb = new Image(orbDrawable());
            orb.setScaling(Scaling.fit);
            Label text = new Label(lines.get(i), skin, style);
            text.setAlignment(Align.left);
            text.setColor(BODY_BROWN);
            text.setWrap(true);
            text.setFontScale(scale);
            Table row = new Table();
            row.left();
            row.add(orb).size(ORB_SIZE, ORB_SIZE).padRight(22f).center();
            row.add(text).growX().left();
            list.add(row).growX().left().padBottom(i == lines.size() - 1 ? 0f : ROW_GAP).row();
        }
        return list;
    }

    private static List<String> objectives(LevelConfig level) {
        List<String> lines = new ArrayList<>();
        LevelType type = level.getType();
        if (type == null) {
            type = LevelType.NORMAL;
        }
        switch (type) {
            case CONVEYOR_BELT -> lines.add("Plant what arrives on the conveyor.");
            case LOCKED_PLANTS -> lines.add("Some plants are locked this level.");
            case SAVE_OUR_SEEDS -> lines.add("Protect the marked plants.");
            case TIMED_WAR -> {
                if ("timed-sun".equals(level.getSpecialHandlerKey())) {
                    lines.add("Produce 150 sun within 60 seconds.");
                } else {
                    lines.add("Defeat 3 zombies within 60 seconds.");
                }
            }
            case NIGHT_OPS -> lines.add("No sky sun. Plan your producers.");
            case DEAD_LINE -> lines.add("Don't let the zombies cross the marked line.");
            case LOVE_YOUR_PLANTS -> lines.add("Don't lose too many plants.");
            case PLANT_WHAT_YOU_GET -> {
                lines.add("Plant freely with " + PlantWhatYouGetHandler.DEFAULT_STARTING_SUN + " starting sun.");
                lines.add("No sunflowers. Start the waves when you are ready.");
            }
            case BOSS -> lines.add("Defeat the boss.");
            default -> lines.add("Don't let the zombies reach your house.");
        }
        if (type != LevelType.TIMED_WAR && type != LevelType.BOSS && type != LevelType.PLANT_WHAT_YOU_GET) {
            int waves = level.getWaveCount();
            if (waves > 1) {
                lines.add("Survive all " + waves + " waves of zombies.");
            }
        }
        return lines;
    }

    private static Drawable borderPanel(Skin skin, GameAssets assets) {
        if (skin.has(BORDER_SKIN, Drawable.class)) {
            return skin.getDrawable(BORDER_SKIN);
        }
        return patch(assets, MenuAssetIds.STORE_PANEL_BORDER, 48, 48, 48, 48);
    }

    private static Drawable innerPanel(Skin skin, GameAssets assets) {
        if (skin.has(INNER_SKIN, Drawable.class)) {
            return skin.getDrawable(INNER_SKIN);
        }
        return patch(assets, LawnAssetIds.DIALOG_INNER, 16, 16, 16, 16);
    }

    private static NinePatchDrawable patch(GameAssets assets, String imageId, int left, int right, int top, int bottom) {
        return new NinePatchDrawable(new NinePatch(assets.region(imageId), left, right, top, bottom));
    }

    private static Drawable dimDrawable() {
        if (dimTexture == null) {
            Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixmap.setColor(Color.WHITE);
            pixmap.fill();
            dimTexture = new Texture(pixmap);
            pixmap.dispose();
        }
        return new TextureRegionDrawable(new TextureRegion(dimTexture));
    }

    private static Drawable headerDrawable() {
        if (headerTexture == null) {
            headerTexture = headerBarTexture();
        }
        return new NinePatchDrawable(new NinePatch(headerTexture, 12, 12, 12, 12));
    }

    private static Drawable orbDrawable() {
        if (orbTexture == null) {
            orbTexture = orbTexture();
        }
        return new TextureRegionDrawable(new TextureRegion(orbTexture));
    }

    private static Texture headerBarTexture() {
        int size = 48;
        float radius = 12f;
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
        Color top = rgb(255, 214, 64);
        Color bottom = rgb(244, 154, 28);
        Color mix = new Color();
        for (int y = 0; y < size; y++) {
            float along = y / (float) (size - 1);
            mix.set(top).lerp(bottom, along);
            for (int x = 0; x < size; x++) {
                float dx = 0f;
                if (x < radius) {
                    dx = radius - x;
                } else if (x > size - 1 - radius) {
                    dx = x - (size - 1 - radius);
                }
                float dy = y < radius ? radius - y : 0f;
                if (dx * dx + dy * dy > radius * radius) {
                    pixmap.drawPixel(x, y, 0);
                    continue;
                }
                pixmap.drawPixel(x, y, Color.rgba8888(mix));
            }
        }
        Texture texture = new Texture(pixmap);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pixmap.dispose();
        return texture;
    }

    private static Texture orbTexture() {
        int size = 64;
        float cx = (size - 1) * 0.5f;
        float cy = (size - 1) * 0.5f;
        float radius = size * 0.46f;
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
        Color highlight = rgb(255, 255, 255);
        Color core = rgb(244, 244, 246);
        Color shade = rgb(168, 170, 176);
        Color rim = rgb(132, 134, 140);
        Color mix = new Color();
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float dx = x - cx;
                float dy = y - cy;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                float cover = MathUtils.clamp((radius - dist) / 1.35f, 0f, 1f);
                if (cover <= 0.004f) {
                    pixmap.drawPixel(x, y, 0);
                    continue;
                }
                float nx = dx / radius;
                float ny = dy / radius;
                float light = MathUtils.clamp(0.78f - nx * 0.22f - ny * 0.52f, 0f, 1f);
                float specX = nx + 0.32f;
                float specY = ny + 0.38f;
                float spec = (float) Math.exp(-(specX * specX + specY * specY) / 0.055f);
                mix.set(shade).lerp(core, 0.28f + light * 0.72f);
                mix.lerp(highlight, spec * 0.85f);
                float edge = MathUtils.clamp((dist - radius * 0.78f) / (radius * 0.22f), 0f, 1f);
                mix.lerp(rim, edge * 0.45f);
                mix.a = cover;
                pixmap.drawPixel(x, y, Color.rgba8888(mix));
            }
        }
        Texture texture = new Texture(pixmap);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pixmap.dispose();
        return texture;
    }

    private static Color rgb(int r, int g, int b) {
        return new Color(r / 255f, g / 255f, b / 255f, 1f);
    }

    private static String outlineStyle(Skin skin) {
        if (skin.has("big_outline", Label.LabelStyle.class)) {
            return "big_outline";
        }
        if (skin.has("medium_outline", Label.LabelStyle.class)) {
            return "medium_outline";
        }
        return "big";
    }

    private static String bodyStyle(Skin skin) {
        if (skin.has("big", Label.LabelStyle.class)) {
            return "big";
        }
        if (skin.has("medium", Label.LabelStyle.class)) {
            return "medium";
        }
        return "default";
    }
}
