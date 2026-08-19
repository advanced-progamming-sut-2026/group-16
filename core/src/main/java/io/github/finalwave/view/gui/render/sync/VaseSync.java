package io.github.finalwave.view.gui.render.sync;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.entity.Vase;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.render.ActorRegistry;
import io.github.finalwave.view.gui.render.LawnLayout;
import io.github.finalwave.view.gui.widget.PamActor;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;


public final class VaseSync {
    public static final String BROWN_PAM = "768/FULL/VASEBREAKER/VASE_BROWN/VASE_BROWN.PAM";
    public static final String GREEN_PAM = "768/FULL/VASEBREAKER/VASE_GREEN/VASE_GREEN.PAM";
    public static final String GARGANTUAR_PAM = "768/FULL/VASEBREAKER/VASE_GARGANTUAR/VASE_GARGANTUAR.PAM";
    public static final String IDLE_CLIP = "idle";

    public static final String BROWN_BURST = "IMAGE_VASEBREAKER_VASE_BROWN_VASE_BROWN_293X263";
    public static final String BROWN_GLOW = "IMAGE_VASEBREAKER_VASE_BROWN_VASE_BROWN_68X51";
    public static final String GREEN_BURST = "IMAGE_VASEBREAKER_VASE_GREEN_VASE_GREEN_293X263";
    public static final String GREEN_GLOW = "IMAGE_VASEBREAKER_VASE_GREEN_VASE_GREEN_68X51";
    public static final String GARGANTUAR_BURST = "IMAGE_VASEBREAKER_VASE_GARGANTUAR_VASE_GARGANTUAR_293X263";
    public static final String GARGANTUAR_GLOW = "IMAGE_VASEBREAKER_VASE_GARGANTUAR_VASE_GARGANTUAR_68X51";
    public static final String[] SMASH_IMAGES = {
            BROWN_BURST, BROWN_GLOW,
            GREEN_BURST, GREEN_GLOW,
            GARGANTUAR_BURST, GARGANTUAR_GLOW
    };

    private static final float BURST_TILE_WIDTH = 1.7f;
    private static final float BURST_FADE_SECONDS = 0.18f;
    private static final float BURST_SCALE = 1.12f;
    private static final float GLOW_TILE_WIDTH = 0.7f;
    private static final float GLOW_DROP_RATIO = 0.15f;
    private static final float GLOW_DELAY_SECONDS = 0.05f;
    private static final float GLOW_HOLD_SECONDS = 0.45f;
    private static final float GLOW_FADE_SECONDS = 0.4f;
    private static final float GLOW_SCALE = 1.45f;
    private static final float VASE_FADE_SECONDS = 0.12f;

    private final GameAssets assets;
    private final LawnLayout layout;
    private final Group layer;
    private final ActorRegistry<Vase, PamActor> vases = new ActorRegistry<>();
    private final Map<PamActor, Vase.Content> contents = new IdentityHashMap<>();

    public VaseSync(GameAssets assets, LawnLayout layout, Group layer) {
        this.assets = assets;
        this.layout = layout;
        this.layer = layer;
    }

    public void sync(GameSession session) {
        if (session == null) {
            return;
        }
        List<Vase> live = new ArrayList<>();
        for (Vase vase : session.getVases()) {
            if (vase != null && vase.isAlive()) {
                live.add(vase);
            }
        }
        vases.sync(live, this::spawn, this::update, this::smash);
    }

    public void clear() {
        contents.clear();
        vases.clear(actor -> {
            actor.clearActions();
            actor.remove();
        });
    }

    private PamActor spawn(Vase vase) {
        PamActor actor = new PamActor(assets.pamPlayer());
        actor.setTouchable(Touchable.disabled);
        actor.setAnchor(0.5f, LawnLayout.VASE_ANCHOR_Y);
        contents.put(actor, vase.getContent());
        layer.addActor(actor);
        return actor;
    }

    private void update(Vase vase, PamActor actor) {
        Vector2 center = layout.cellCenter(vase.getCol(), vase.getRow());
        actor.setSize(layout.tileWidth(), layout.tileHeight());
        actor.setPosition(center.x - actor.getWidth() / 2f, center.y - actor.getHeight() / 2f);
        actor.setClip(pamPath(vase), IDLE_CLIP, LawnLayout.VASE_SCALE, true);
        actor.setUserObject(vase.getRow());
        contents.put(actor, vase.getContent());
    }

    private void smash(PamActor actor) {
        Vase.Content content = contents.remove(actor);
        if (content == null) {
            actor.clearActions();
            actor.remove();
            return;
        }
        Vector2 center = new Vector2(
                actor.getX() + actor.getWidth() / 2f,
                actor.getY() + actor.getHeight() / 2f);
        int vaseZ = actor.getZIndex();
        Group burst = smashSprite(burstId(content), layout.tileWidth() * BURST_TILE_WIDTH);
        placeCentered(burst, center.x, center.y);
        layer.addActorAt(vaseZ, burst);
        burst.addAction(Actions.sequence(
                Actions.parallel(
                        Actions.fadeOut(BURST_FADE_SECONDS, Interpolation.fade),
                        Actions.scaleTo(BURST_SCALE, BURST_SCALE, BURST_FADE_SECONDS, Interpolation.sineOut)),
                Actions.removeActor()));
        Group glow = smashSprite(glowId(content), layout.tileWidth() * GLOW_TILE_WIDTH);
        placeCentered(glow, center.x, center.y - layout.tileHeight() * GLOW_DROP_RATIO);
        layer.addActorAt(actor.getZIndex(), glow);
        glow.getColor().a = 0f;
        glow.addAction(Actions.sequence(
                Actions.delay(GLOW_DELAY_SECONDS),
                Actions.alpha(1f, 0.08f),
                Actions.delay(GLOW_HOLD_SECONDS),
                Actions.parallel(
                        Actions.fadeOut(GLOW_FADE_SECONDS, Interpolation.fade),
                        Actions.scaleTo(GLOW_SCALE, GLOW_SCALE, GLOW_FADE_SECONDS, Interpolation.sineOut)),
                Actions.removeActor()));
        actor.addAction(Actions.sequence(
                Actions.fadeOut(VASE_FADE_SECONDS, Interpolation.fade),
                Actions.removeActor()));
    }

    private Group smashSprite(String imageId, float width) {
        TextureRegion region = assets.region(imageId);
        float height = width * region.getRegionHeight() / (float) Math.max(1, region.getRegionWidth());
        Image image = new Image(new TextureRegionDrawable(region));
        image.setTouchable(Touchable.disabled);
        image.setSize(width, height);
        Group sprite = new Group();
        sprite.setTransform(true);
        sprite.setTouchable(Touchable.disabled);
        sprite.setSize(width, height);
        sprite.setOrigin(Align.center);
        sprite.addActor(image);
        return sprite;
    }

    private static void placeCentered(Group sprite, float centerX, float centerY) {
        sprite.setPosition(centerX - sprite.getWidth() / 2f, centerY - sprite.getHeight() / 2f);
    }

    private static String pamPath(Vase vase) {
        return switch (vase.getContent()) {
            case GARGANTUAR -> GARGANTUAR_PAM;
            case PLANT_SEED -> GREEN_PAM;
            case EMPTY, ZOMBIE -> BROWN_PAM;
        };
    }

    private static String burstId(Vase.Content content) {
        return switch (content) {
            case GARGANTUAR -> GARGANTUAR_BURST;
            case PLANT_SEED -> GREEN_BURST;
            case EMPTY, ZOMBIE -> BROWN_BURST;
        };
    }

    private static String glowId(Vase.Content content) {
        return switch (content) {
            case GARGANTUAR -> GARGANTUAR_GLOW;
            case PLANT_SEED -> GREEN_GLOW;
            case EMPTY, ZOMBIE -> BROWN_GLOW;
        };
    }
}
