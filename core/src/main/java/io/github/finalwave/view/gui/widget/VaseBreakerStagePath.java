package io.github.finalwave.view.gui.widget;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import io.github.finalwave.controller.MiniGameHubController;
import io.github.finalwave.model.adventure.ChapterId;
import io.github.finalwave.view.gui.assets.AdventureAssetIds;
import io.github.finalwave.view.gui.assets.GameAssets;
import pvz.libpvz.textures.ResourceIndex;

import java.util.List;
import java.util.Set;
import java.util.function.IntConsumer;


public final class VaseBreakerStagePath extends Group {
    private static final float NODE = 72f;
    private static final float NODE_SCALE = 0.62f;
    private static final float LOCK_SIZE = 28f;
    private static final float ISLAND_H = 200f;
    private static final float ISLAND_W = 210f;
    private static final float PLATFORM_FROM_TOP = 0.16f;
    private static final float[] NODE_X = {380f, 760f, 1140f};
    private static final float[] NODE_Y = {560f, 320f, 560f};

    public VaseBreakerStagePath(GameAssets assets,
                                Skin skin,
                                ChapterId chapter,
                                List<MiniGameHubController.StageInfo> stages,
                                Set<Integer> seenCompleted,
                                Set<Integer> seenUnlocked,
                                IntConsumer onStage) {
        setSize(1920f, 1080f);
        if (assets == null || stages == null || stages.isEmpty()) {
            return;
        }
        ChapterId islandChapter = chapter == null ? ChapterId.ANCIENT_EGYPT : chapter;
        for (String pam : AdventureAssetIds.nodePams()) {
            assets.pamPlayer().loadAsync(pam, () -> {
            });
        }
        int currentIndex = firstOpenIncomplete(stages);
        int count = Math.min(stages.size(), NODE_X.length);
        for (int i = 0; i < count; i++) {
            addActor(island(assets, layoutFor(assets, islandChapter, i)));
        }
        for (int i = 0; i < count - 1; i++) {
            addActor(connector(assets, i, stages.get(i).completed()));
        }
        for (int i = 0; i < count; i++) {
            MiniGameHubController.StageInfo stage = stages.get(i);
            boolean unlocked = stage.playable() && stage.implemented();
            boolean current = unlocked && !stage.completed() && stage.index() == currentIndex;
            boolean playComplete = stage.completed()
                    && seenCompleted != null
                    && !seenCompleted.contains(stage.index());
            boolean playUnlock = unlocked
                    && !stage.completed()
                    && seenUnlocked != null
                    && !seenUnlocked.contains(stage.index());
            addActor(node(assets, skin, layoutFor(assets, islandChapter, i), stage, unlocked, current,
                    playComplete, playUnlock, seenCompleted, seenUnlocked, onStage));
        }
    }

    private static int firstOpenIncomplete(List<MiniGameHubController.StageInfo> stages) {
        for (MiniGameHubController.StageInfo stage : stages) {
            if (stage.playable() && stage.implemented() && !stage.completed()) {
                return stage.index();
            }
        }
        return stages.getLast().index();
    }

    private static SlotLayout layoutFor(GameAssets assets, ChapterId chapter, int slot) {
        int index = Math.min(slot, NODE_X.length - 1);
        String islandId = AdventureAssetIds.levelIsland(chapter, index, false);
        float[] size = islandSize(assets, islandId);
        float cx = NODE_X[index] + NODE / 2f;
        float cy = NODE_Y[index] + NODE / 2f;
        float islandX = cx - size[0] / 2f;
        float islandY = cy - size[1] * (1f - PLATFORM_FROM_TOP);
        return new SlotLayout(islandId, cx, cy, size[0], size[1], islandX, islandY);
    }

    private static float[] islandSize(GameAssets assets, String imageId) {
        float aw = 140f;
        float ah = 140f;
        ResourceIndex.ImageEntry entry = assets.resourceIndex().image(imageId);
        if (entry != null) {
            aw = Math.max(1, entry.aw);
            ah = Math.max(1, entry.ah);
        }
        float scale = Math.min(ISLAND_W / aw, ISLAND_H / ah);
        return new float[] {aw * scale, ah * scale};
    }

    private static Image connector(GameAssets assets, int fromIndex, boolean filled) {
        float x1 = NODE_X[fromIndex] + NODE / 2f;
        float y1 = NODE_Y[fromIndex] + NODE / 2f;
        float x2 = NODE_X[fromIndex + 1] + NODE / 2f;
        float y2 = NODE_Y[fromIndex + 1] + NODE / 2f;
        float dx = x2 - x1;
        float dy = y2 - y1;
        float length = Vector2.len(dx, dy);
        Image image = new Image(new TextureRegionDrawable(assets.region(
                filled ? AdventureAssetIds.CONNECTOR_FILL : AdventureAssetIds.CONNECTOR_EMPTY)));
        image.setSize(length, 28f);
        image.setOrigin(Align.center);
        image.setPosition((x1 + x2) / 2f - length / 2f, (y1 + y2) / 2f - 14f);
        image.setRotation(MathUtils.atan2(dy, dx) * MathUtils.radiansToDegrees);
        image.setColor(filled ? Color.WHITE : new Color(1f, 1f, 1f, 0.45f));
        return image;
    }

    private static Image island(GameAssets assets, SlotLayout layout) {
        Image image = new Image(new TextureRegionDrawable(assets.region(layout.islandId)));
        image.setScaling(Scaling.fit);
        image.setSize(layout.islandW, layout.islandH);
        image.setPosition(layout.islandX, layout.islandY);
        return image;
    }

    private static ActorNode node(GameAssets assets,
                                  Skin skin,
                                  SlotLayout layout,
                                  MiniGameHubController.StageInfo stage,
                                  boolean unlocked,
                                  boolean current,
                                  boolean playComplete,
                                  boolean playUnlock,
                                  Set<Integer> seenCompleted,
                                  Set<Integer> seenUnlocked,
                                  IntConsumer onStage) {
        AdventureAssetIds.NodePlayback playback = AdventureAssetIds.levelNodePlayback(
                stage.completed(), unlocked, playUnlock, playComplete);
        String labelStyle = skin.has("medium_outline", Label.LabelStyle.class) ? "medium_outline" : "medium";
        Label number = new Label(String.valueOf(stage.index()), skin, labelStyle);
        number.setAlignment(Align.center);
        number.setFontScale(0.7f);

        Table overlay = new Table();
        overlay.top();
        overlay.add(number).padTop(2).row();
        overlay.add().expand();

        Stack stack = new Stack();
        stack.setSize(NODE, NODE);
        PamActor pam = new PamActor(assets.pamPlayer());
        pam.setSize(NODE, NODE);
        pam.setAnchor(0.5f, 0.5f);
        pam.setTouchable(Touchable.disabled);
        if (playback.thenClip() != null) {
            pam.playThen(playback.pam(), playback.clip(), NODE_SCALE, playback.thenClip(), true, () -> {
                if (playComplete && seenCompleted != null) {
                    seenCompleted.add(stage.index());
                }
                if (seenUnlocked != null) {
                    seenUnlocked.add(stage.index());
                }
            });
        } else {
            pam.setClip(playback.pam(), playback.clip(), NODE_SCALE, playback.loop());
        }
        stack.add(pam);
        stack.add(overlay);
        if (stage.locked() || !stage.implemented()) {
            Image lock = new Image(new TextureRegionDrawable(assets.region(AdventureAssetIds.LOCK)));
            lock.setScaling(Scaling.fit);
            Table lockPad = new Table();
            lockPad.add(lock).size(LOCK_SIZE, LOCK_SIZE);
            stack.add(lockPad);
        }

        ActorNode wrapper = new ActorNode(stack);
        wrapper.setSize(NODE, NODE);
        wrapper.setPosition(layout.cx - NODE / 2f, layout.cy - NODE / 2f);
        PvzButtons.animate(wrapper, current ? 1.1f : 1.06f, 0.92f, () -> {
            if (onStage != null && unlocked) {
                onStage.accept(stage.index());
            }
        });
        return wrapper;
    }

    private static final class SlotLayout {
        private final String islandId;
        private final float cx;
        private final float cy;
        private final float islandW;
        private final float islandH;
        private final float islandX;
        private final float islandY;

        private SlotLayout(String islandId,
                           float cx,
                           float cy,
                           float islandW,
                           float islandH,
                           float islandX,
                           float islandY) {
            this.islandId = islandId;
            this.cx = cx;
            this.cy = cy;
            this.islandW = islandW;
            this.islandH = islandH;
            this.islandX = islandX;
            this.islandY = islandY;
        }
    }

    private static final class ActorNode extends Group {
        private ActorNode(Stack stack) {
            addActor(stack);
            stack.setFillParent(true);
            setTransform(true);
        }
    }
}
