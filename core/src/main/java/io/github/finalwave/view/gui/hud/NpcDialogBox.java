package io.github.finalwave.view.gui.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.assets.LawnAssetIds;
import pvz.libpvz.pam.PamPlayer;

import java.util.List;


public final class NpcDialogBox extends WidgetGroup {
    private static final float DIM_ALPHA = 0.55f;
    private static final float DAVE_X = 340f;
    private static final float PENNY_X = 1400f;
    private static final float CHAR_Y = 300f;
    private static final float BUBBLE_W = 380f;
    private static final float BUBBLE_H = 210f;
    private static final float BUBBLE_SHIFT_X = 40f;
    private static final float BUBBLE_SHIFT_Y = 300f;
    private static final float TEXT_PAD_X = 46f;
    private static final float TEXT_PAD_TOP = 40f;
    private static final float TEXT_PAD_BOTTOM = 62f;
    private static final float TEXT_SCALE = 0.9f;
    private static final Color TEXT_COLOR = new Color(0.20f, 0.12f, 0.05f, 1f);

    private static Texture dimPixel;

    private final GameAssets assets;
    private final BitmapFont font;
    private final GlyphLayout glyph = new GlyphLayout();
    private final Color oldBatch = new Color();

    private List<NpcDialogLine> lines = List.of();
    private int index;
    private float talkTime;
    private Runnable onFinished;

    public NpcDialogBox(GameAssets assets) {
        this.assets = assets;
        this.font = fontOf(assets);
        setFillParent(true);
        setTouchable(Touchable.enabled);
        setVisible(false);
        addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                advance();
            }
        });
    }

    public void setOnFinished(Runnable onFinished) {
        this.onFinished = onFinished;
    }

    public boolean isShowing() {
        return isVisible();
    }

    public void show(List<NpcDialogLine> script) {
        lines = script == null ? List.of() : List.copyOf(script);
        index = 0;
        talkTime = 0f;
        if (lines.isEmpty()) {
            hide();
            return;
        }
        assets.pamPlayer().loadSync(LawnAssetIds.CRAZY_DAVE_PAM);
        assets.pamPlayer().loadSync(LawnAssetIds.PENNY_PAM);
        assets.region(LawnAssetIds.SPEECH_BUBBLE);
        setVisible(true);
        toFront();
    }

    public void hide() {
        lines = List.of();
        index = 0;
        talkTime = 0f;
        setVisible(false);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (isVisible()) {
            talkTime += delta;
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (!isVisible()) {
            return;
        }
        oldBatch.set(batch.getColor());
        batch.setColor(0f, 0f, 0f, DIM_ALPHA * parentAlpha * getColor().a);
        batch.draw(dimPixel(), 0f, 0f, getWidth(), getHeight());
        batch.setColor(1f, 1f, 1f, parentAlpha * getColor().a);

        NpcDialogLine line = current();
        boolean daveTalks = line == null || line.speaker() != NpcSpeaker.PENNY;
        PamPlayer player = assets.pamPlayer();
        player.draw(batch, LawnAssetIds.CRAZY_DAVE_PAM,
                daveTalks ? LawnAssetIds.SPEAK_CLIP : LawnAssetIds.IDLE_CLIP,
                talkTime, DAVE_X, CHAR_Y, true);
        player.draw(batch, LawnAssetIds.PENNY_PAM,
                daveTalks ? LawnAssetIds.IDLE_CLIP : LawnAssetIds.SPEAK_CLIP,
                talkTime, PENNY_X, CHAR_Y, true);

        if (line != null) {
            drawBubble(batch, line, daveTalks, parentAlpha);
        }
        batch.setColor(oldBatch);
        super.draw(batch, parentAlpha);
    }

    private void advance() {
        if (!isVisible() || lines.isEmpty()) {
            return;
        }
        index++;
        talkTime = 0f;
        if (index >= lines.size()) {
            Runnable finished = onFinished;
            hide();
            if (finished != null) {
                finished.run();
            }
        }
    }

    private NpcDialogLine current() {
        if (index < 0 || index >= lines.size()) {
            return null;
        }
        return lines.get(index);
    }

    private void drawBubble(Batch batch, NpcDialogLine line, boolean daveTalks, float parentAlpha) {
        float bubbleX = daveTalks
                ? DAVE_X + BUBBLE_SHIFT_X
                : PENNY_X - BUBBLE_SHIFT_X - BUBBLE_W;
        float bubbleY = CHAR_Y + BUBBLE_SHIFT_Y;
        TextureRegion bubble = assets.region(LawnAssetIds.SPEECH_BUBBLE);
        batch.setColor(1f, 1f, 1f, parentAlpha * getColor().a);
        if (daveTalks) {
            batch.draw(bubble, bubbleX, bubbleY, BUBBLE_W, BUBBLE_H);
        } else {
            batch.draw(bubble, bubbleX + BUBBLE_W, bubbleY, -BUBBLE_W, BUBBLE_H);
        }

        float textX = bubbleX + TEXT_PAD_X;
        float textW = BUBBLE_W - TEXT_PAD_X * 2f;
        float areaTop = bubbleY + BUBBLE_H - TEXT_PAD_TOP;
        float areaH = BUBBLE_H - TEXT_PAD_TOP - TEXT_PAD_BOTTOM;
        float oldX = font.getData().scaleX;
        float oldY = font.getData().scaleY;
        font.getData().setScale(TEXT_SCALE);
        glyph.setText(font, line.text() == null ? "" : line.text(), TEXT_COLOR, textW, Align.center, true);
        float textY = areaTop - Math.max(0f, (areaH - glyph.height) * 0.5f);
        font.draw(batch, glyph, textX, textY);
        font.getData().setScale(oldX, oldY);
    }

    private static Texture dimPixel() {
        if (dimPixel == null) {
            Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixmap.setColor(Color.WHITE);
            pixmap.fill();
            dimPixel = new Texture(pixmap);
            pixmap.dispose();
        }
        return dimPixel;
    }

    private static BitmapFont fontOf(GameAssets assets) {
        if (assets.skin().has("medium", Label.LabelStyle.class)) {
            return assets.skin().get("medium", Label.LabelStyle.class).font;
        }
        if (assets.skin().has("default", Label.LabelStyle.class)) {
            return assets.skin().get("default", Label.LabelStyle.class).font;
        }
        return new BitmapFont();
    }
}
