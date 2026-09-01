package io.github.finalwave.view.gui.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
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
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.finalwave.network.match.MatchReactions;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.assets.StickerReactionCatalog;
import io.github.finalwave.view.gui.widget.PvzButtons;
import io.github.finalwave.view.gui.widget.ShopItemCard;
import io.github.finalwave.view.gui.widget.StoreChrome;

import java.util.function.BiConsumer;

public final class ReactionDrawer {
    private static final float SLIDE_SECONDS = 0.22f;
    private static final float WIDTH_RATIO = 0.28f;
    private static final float MAX_WIDTH = 360f;
    private static final Color PILL_FILL = new Color(0.98f, 0.98f, 0.96f, 1f);

    private static Group root;
    private static Table drawer;
    private static Texture dimTexture;
    private static Drawable pillDrawable;
    private static Runnable onClosedCallback;

    private ReactionDrawer() {
    }

    public static void toggle(Table modalLayer,
                              Viewport viewport,
                              GameAssets assets,
                              BiConsumer<String, Integer> send,
                              Runnable onClosed) {
        if (isOpen()) {
            close(modalLayer);
            return;
        }
        open(modalLayer, viewport, assets, send, onClosed);
    }

    public static void close(Table modalLayer) {
        if (!isOpen() || drawer == null) {
            cleanup();
            return;
        }
        float targetX = root.getWidth();
        drawer.clearActions();
        drawer.addAction(Actions.sequence(
                Actions.moveTo(targetX, drawer.getY(), SLIDE_SECONDS, Interpolation.swingIn),
                Actions.run(() -> {
                    if (root != null && root.getParent() != null) {
                        root.remove();
                    }
                    Runnable callback = onClosedCallback;
                    cleanup();
                    if (callback != null) {
                        callback.run();
                    }
                })));
    }

    public static boolean isOpen() {
        return root != null && root.getParent() != null;
    }

    private static void open(Table modalLayer,
                             Viewport viewport,
                             GameAssets assets,
                             BiConsumer<String, Integer> send,
                             Runnable onClosed) {
        if (modalLayer == null || viewport == null || assets == null || send == null) {
            return;
        }
        cleanup();
        onClosedCallback = onClosed;

        float screenW = viewport.getWorldWidth();
        float screenH = viewport.getWorldHeight();
        float drawerW = Math.min(screenW * WIDTH_RATIO, MAX_WIDTH);

        root = new Group();
        root.setSize(screenW, screenH);
        root.setTouchable(Touchable.childrenOnly);

        Image dimmer = new Image(dimDrawable());
        dimmer.setFillParent(true);
        dimmer.setColor(1f, 1f, 1f, 0.35f);
        dimmer.setTouchable(Touchable.enabled);
        dimmer.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                close(modalLayer);
                return true;
            }
        });
        root.addActor(dimmer);

        Skin skin = assets.skin();
        drawer = new Table();
        drawer.setTouchable(Touchable.childrenOnly);
        drawer.setBackground(StoreChrome.drawerPanel());
        drawer.pad(16f, 12f, 16f, 10f);
        drawer.top();

        Table stickers = new Table();
        stickers.defaults().pad(ReactionUiMetrics.DRAWER_CELL_PAD);
        String[] stickerPlants = MatchReactions.stickers();
        for (int i = 0; i < stickerPlants.length; i++) {
            int index = i;
            stickers.add(stickerCard(assets, stickerPlants[index], index,
                            () -> select(modalLayer, send, MatchReactions.STICKER, index)))
                    .size(ReactionUiMetrics.DRAWER_STICKER_SIZE);
        }
        drawer.add(stickers).growX().padBottom(ReactionUiMetrics.DRAWER_STICKER_ROW_PAD_BOTTOM).row();

        Table emojis = new Table();
        emojis.defaults().pad(ReactionUiMetrics.DRAWER_CELL_PAD);
        for (int i = 0; i < MatchReactions.emojiCount(); i++) {
            int index = i;
            emojis.add(emojiButton(index, () -> select(modalLayer, send, MatchReactions.EMOJI, index)))
                    .size(ReactionUiMetrics.DRAWER_EMOJI_SIZE);
        }
        drawer.add(emojis).growX().padBottom(ReactionUiMetrics.DRAWER_EMOJI_ROW_PAD_BOTTOM).row();

        Table texts = new Table();
        texts.defaults().pad(ReactionUiMetrics.DRAWER_CELL_PAD).growX().uniformX();
        String[] messages = MatchReactions.messages();
        for (int i = 0; i < messages.length; i++) {
            int index = i;
            if (i > 0 && i % 2 == 0) {
                texts.row();
            }
            texts.add(textPill(skin, messages[i], () -> select(modalLayer, send, MatchReactions.TEXT, index)))
                    .height(ReactionUiMetrics.DRAWER_TEXT_PILL_HEIGHT)
                    .growX();
        }
        drawer.add(texts).growX();

        drawer.pack();
        float drawerH = drawer.getPrefHeight();
        float startX = screenW;
        float endX = screenW - drawerW;
        float y = (screenH - drawerH) * 0.5f;
        drawer.setSize(drawerW, drawerH);
        drawer.setPosition(startX, y);
        root.addActor(drawer);

        modalLayer.addActor(root);
        drawer.addAction(Actions.moveTo(endX, y, SLIDE_SECONDS, Interpolation.swingOut));
    }

    private static void select(Table modalLayer, BiConsumer<String, Integer> send, String kind, int index) {
        send.accept(kind, index);
        close(modalLayer);
    }

    private static Actor stickerCard(GameAssets assets, String plantName, int index, Runnable onClick) {
        Stack stack = new Stack();
        Image cap = new Image(StoreChrome.brownButton());
        cap.setScaling(com.badlogic.gdx.utils.Scaling.stretch);
        stack.add(cap);
        float iconSize = ReactionUiMetrics.DRAWER_STICKER_SIZE - 16f;
        Actor art = StickerReactionCatalog.createActor(assets, index, iconSize);
        if (art == null) {
            art = ShopItemCard.plantArt(assets, plantName, 0.42f);
        }
        art.setTouchable(Touchable.disabled);
        Table iconWrap = new Table();
        iconWrap.add(art).size(iconSize, iconSize).center();
        stack.add(iconWrap);
        stack.setTransform(true);
        stack.setOrigin(Align.center);
        PvzButtons.animate(stack, 1.1f, 0.92f, onClick);
        return stack;
    }

    private static Actor emojiButton(int index, Runnable onClick) {
        Stack stack = new Stack();
        Image cap = new Image(StoreChrome.brownButton());
        cap.setScaling(com.badlogic.gdx.utils.Scaling.stretch);
        stack.add(cap);
        Actor icon = ReactionEmojiCatalog.iconFor(index, ReactionUiMetrics.DRAWER_EMOJI_ICON_SIZE);
        icon.setTouchable(Touchable.disabled);
        Table iconWrap = new Table();
        iconWrap.add(icon).center()
                .padLeft(ReactionUiMetrics.DRAWER_EMOJI_ICON_OFFSET_X)
                .padBottom(ReactionUiMetrics.DRAWER_EMOJI_ICON_OFFSET_Y);
        stack.add(iconWrap);
        stack.setTransform(true);
        stack.setOrigin(Align.center);
        PvzButtons.animate(stack, 1.1f, 0.92f, onClick);
        return stack;
    }

    private static Actor textPill(Skin skin, String text, Runnable onClick) {
        Stack stack = new Stack();
        Image background = new Image(pillBackground());
        background.setScaling(com.badlogic.gdx.utils.Scaling.stretch);
        stack.add(background);
        String style = skin.has("medium_outline", Label.LabelStyle.class) ? "medium_outline" : "medium";
        Label label = new Label(text, skin, style);
        label.setFontScale(ReactionUiMetrics.DRAWER_TEXT_FONT_SCALE);
        label.setAlignment(Align.center);
        label.setTouchable(Touchable.disabled);
        Table labelWrap = new Table();
        labelWrap.add(label).pad(2f, 6f, 2f, 6f);
        stack.add(labelWrap);
        stack.setTransform(true);
        stack.setOrigin(Align.center);
        PvzButtons.animate(stack, 1.04f, 0.96f, onClick);
        return stack;
    }

    private static Drawable pillBackground() {
        if (pillDrawable == null) {
            Pixmap pixmap = new Pixmap(48, 48, Pixmap.Format.RGBA8888);
            pixmap.setColor(PILL_FILL);
            pixmap.fillCircle(24, 24, 22);
            Texture texture = new Texture(pixmap);
            texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            pixmap.dispose();
            NinePatch patch = new NinePatch(texture, 20, 20, 20, 20);
            pillDrawable = new NinePatchDrawable(patch);
        }
        return pillDrawable;
    }

    private static Drawable dimDrawable() {
        if (dimTexture == null) {
            Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixmap.setColor(Color.BLACK);
            pixmap.fill();
            dimTexture = new Texture(pixmap);
            pixmap.dispose();
        }
        return new TextureRegionDrawable(dimTexture);
    }

    private static void cleanup() {
        root = null;
        drawer = null;
        onClosedCallback = null;
    }
}
