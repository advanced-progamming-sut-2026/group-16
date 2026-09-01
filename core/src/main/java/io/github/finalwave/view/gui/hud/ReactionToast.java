package io.github.finalwave.view.gui.hud;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import io.github.finalwave.network.match.MatchReactionPayload;
import io.github.finalwave.network.match.MatchReactions;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.assets.StickerReactionCatalog;
import io.github.finalwave.view.gui.widget.StickerAtlasActor;

public final class ReactionToast extends Table {
    private static final float DISPLAY_SECONDS = 2.8f;
    private static final float FADE_SECONDS = 0.35f;
    private static ReactionToast active;

    private ReactionToast(GameAssets assets, MatchReactionPayload payload) {
        float padding = paddingFor(payload.getKind());
        pad(padding);
        setTransform(true);
        setOrigin(Align.top | Align.center);
        getColor().a = 0f;
        setScale(1.06f);

        Skin skin = assets.skin();
        String kind = payload.getKind();
        Actor body = bodyFor(assets, skin, payload);
        if (body != null) {
            if (MatchReactions.STICKER.equals(kind)) {
                add(body).size(ReactionUiMetrics.TOAST_STICKER_SIZE, ReactionUiMetrics.TOAST_STICKER_SIZE).center().row();
            } else if (MatchReactions.EMOJI.equals(kind)) {
                add(body).size(ReactionUiMetrics.TOAST_EMOJI_SIZE, ReactionUiMetrics.TOAST_EMOJI_SIZE).center().row();
            } else {
                add(body).center().row();
            }
        }

        addAction(Actions.sequence(
                Actions.parallel(
                        Actions.fadeIn(0.18f, Interpolation.sineOut),
                        Actions.scaleTo(1f, 1f, 0.18f, Interpolation.sineOut)),
                Actions.delay(DISPLAY_SECONDS),
                Actions.parallel(
                        Actions.fadeOut(FADE_SECONDS),
                        Actions.scaleTo(0.96f, 0.96f, FADE_SECONDS)),
                Actions.run(() -> {
                    if (active == this) {
                        active = null;
                    }
                }),
                Actions.removeActor()));
    }

    public static void show(Table layer,
                            GameAssets assets,
                            MatchReactionPayload payload,
                            float anchorX,
                            float anchorY) {
        if (layer == null || assets == null || payload == null) {
            return;
        }
        dismissActive();
        ReactionToast toast = new ReactionToast(assets, payload);
        active = toast;
        toast.pack();
        float x = anchorX + ReactionUiMetrics.TOAST_OFFSET_X - toast.getWidth() * 0.5f;
        float y = anchorY + ReactionUiMetrics.TOAST_OFFSET_Y - toast.getHeight();
        toast.setPosition(x, y);
        layer.addActor(toast);
    }

    private static float paddingFor(String kind) {
        if (MatchReactions.STICKER.equals(kind)) {
            return ReactionUiMetrics.TOAST_PADDING;
        }
        return ReactionUiMetrics.TOAST_COMPACT_PADDING;
    }

    private static void dismissActive() {
        if (active == null) {
            return;
        }
        active.clearActions();
        active.remove();
        active = null;
    }

    private static Actor bodyFor(GameAssets assets, Skin skin, MatchReactionPayload payload) {
        String kind = payload.getKind();
        int index = payload.getIndex();
        if (MatchReactions.STICKER.equals(kind)) {
            return stickerBody(assets, index);
        }
        if (MatchReactions.EMOJI.equals(kind)) {
            return emojiBody(index);
        }
        return textBody(skin, MatchReactions.describe(kind, index));
    }

    private static Actor stickerBody(GameAssets assets, int index) {
        StickerAtlasActor actor = StickerReactionCatalog.createActor(assets, index, ReactionUiMetrics.TOAST_STICKER_SIZE);
        if (actor == null) {
            Label fallback = new Label(MatchReactions.describe(MatchReactions.STICKER, index), assets.skin(), "medium");
            fallback.setAlignment(Align.center);
            return fallback;
        }
        Container<StickerAtlasActor> container = new Container<>(actor);
        container.size(ReactionUiMetrics.TOAST_STICKER_SIZE, ReactionUiMetrics.TOAST_STICKER_SIZE);
        container.fill();
        container.setTouchable(Touchable.disabled);
        return container;
    }

    private static Actor emojiBody(int index) {
        return ReactionEmojiCatalog.iconFor(index, ReactionUiMetrics.TOAST_EMOJI_SIZE);
    }

    private static Actor textBody(Skin skin, String text) {
        String style = skin.has("medium_outline", Label.LabelStyle.class) ? "medium_outline" : "medium";
        if (!skin.has(style, Label.LabelStyle.class)) {
            style = "medium";
        }
        Label label = new Label(text, skin, style);
        label.setAlignment(Align.center);
        label.setFontScale(ReactionUiMetrics.TOAST_TEXT_FONT_SCALE);
        label.setWrap(true);
        Table wrap = new Table();
        wrap.add(label).width(ReactionUiMetrics.TOAST_TEXT_MIN_WIDTH).center();
        return wrap;
    }
}
