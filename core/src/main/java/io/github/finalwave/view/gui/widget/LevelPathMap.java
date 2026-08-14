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
import io.github.finalwave.controller.AdventureController;
import io.github.finalwave.model.adventure.ChapterConfig;
import io.github.finalwave.model.adventure.LevelConfig;
import io.github.finalwave.model.adventure.LevelType;
import io.github.finalwave.model.user.ChapterProgress;
import io.github.finalwave.view.gui.assets.AdventureAssetIds;
import io.github.finalwave.view.gui.assets.GameAssets;
import pvz.libpvz.textures.ResourceIndex;

import java.util.List;
import java.util.Set;
import java.util.function.IntConsumer;

public final class LevelPathMap extends Group {
    private static final float NODE = 72f;
    private static final float NODE_SCALE = 0.62f;
    private static final float LOCK_SIZE = 28f;
    private static final float NORMAL_ISLAND_H = 200f;
    private static final float NORMAL_ISLAND_W = 210f;
    private static final float BOSS_ISLAND_H = 360f;
    private static final float BOSS_ISLAND_W = 280f;
    private static final float PLATFORM_FROM_TOP = 0.16f;
    private static final float[] NODE_X = {380f, 760f, 1140f, 1520f};
    private static final float[] NODE_Y = {560f, 320f, 560f, 320f};

    public LevelPathMap(GameAssets assets,
                        Skin skin,
                        AdventureController controller,
                        Set<Integer> seenCompleted,
                        Set<Integer> seenUnlocked,
                        IntConsumer onLevel) {
        setSize(1920f, 1080f);
        if (controller == null || controller.getChapter() == null) {
            return;
        }
        ChapterConfig chapter = controller.getChapter();
        for (String pam : AdventureAssetIds.nodePams()) {
            assets.pamPlayer().loadAsync(pam, () -> {
            });
        }
        ChapterProgress progress = controller.getUser() == null
                ? null
                : controller.getUser().getChapterProgress();
        List<LevelConfig> levels = chapter.getLevels();
        int currentIndex = firstOpenIncomplete(controller, levels, progress);

        for (int i = 0; i < levels.size(); i++) {
            LevelConfig level = levels.get(i);
            boolean boss = level.getType() == LevelType.BOSS;
            SlotLayout layout = layoutFor(assets, chapter, i, boss);
            addActor(island(assets, layout));
        }

        for (int i = 0; i < levels.size() - 1; i++) {
            boolean filled = progress != null && progress.isLevelCompleted(chapter.getId(), levels.get(i).getIndex());
            addActor(connector(assets, i, filled));
        }

        for (int i = 0; i < levels.size(); i++) {
            LevelConfig level = levels.get(i);
            boolean completed = progress != null && progress.isLevelCompleted(chapter.getId(), level.getIndex());
            boolean unlocked = controller.isLevelUnlocked(level.getIndex());
            boolean current = unlocked && !completed && level.getIndex() == currentIndex;
            boolean playComplete = completed && seenCompleted != null && !seenCompleted.contains(level.getIndex());
            boolean playUnlock = unlocked && !completed && seenUnlocked != null && !seenUnlocked.contains(level.getIndex());
            boolean boss = level.getType() == LevelType.BOSS;
            SlotLayout layout = layoutFor(assets, chapter, i, boss);
            addActor(node(assets, skin, layout, level, completed, unlocked, current, boss,
                    playComplete, playUnlock, seenCompleted, seenUnlocked, onLevel));
        }
    }

    private static int firstOpenIncomplete(AdventureController controller,
                                           List<LevelConfig> levels,
                                           ChapterProgress progress) {
        for (LevelConfig level : levels) {
            if (controller.isLevelUnlocked(level.getIndex())
                    && (progress == null || !progress.isLevelCompleted(controller.getChapter().getId(), level.getIndex()))) {
                return level.getIndex();
            }
        }
        return levels.isEmpty() ? 1 : levels.get(levels.size() - 1).getIndex();
    }

    private static SlotLayout layoutFor(GameAssets assets, ChapterConfig chapter, int slot, boolean boss) {
        int index = Math.min(slot, NODE_X.length - 1);
        String islandId = AdventureAssetIds.levelIsland(chapter.getId(), index, boss);
        float[] size = islandSize(assets, islandId, boss);
        float cx = NODE_X[index] + NODE / 2f;
        float cy = NODE_Y[index] + NODE / 2f;
        float islandX = cx - size[0] / 2f;
        float islandY = cy - size[1] * (1f - PLATFORM_FROM_TOP);
        return new SlotLayout(islandId, cx, cy, size[0], size[1], islandX, islandY, boss);
    }

    private static float[] islandSize(GameAssets assets, String imageId, boolean boss) {
        float aw = 140f;
        float ah = 140f;
        ResourceIndex.ImageEntry entry = assets.resourceIndex().image(imageId);
        if (entry != null) {
            aw = Math.max(1, entry.aw);
            ah = Math.max(1, entry.ah);
        }
        float maxW = boss ? BOSS_ISLAND_W : NORMAL_ISLAND_W;
        float maxH = boss ? BOSS_ISLAND_H : NORMAL_ISLAND_H;
        float scale = Math.min(maxW / aw, maxH / ah);
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
                                  LevelConfig level,
                                  boolean completed,
                                  boolean unlocked,
                                  boolean current,
                                  boolean boss,
                                  boolean playComplete,
                                  boolean playUnlock,
                                  Set<Integer> seenCompleted,
                                  Set<Integer> seenUnlocked,
                                  IntConsumer onLevel) {
        AdventureAssetIds.NodePlayback playback = AdventureAssetIds.levelNodePlayback(
                completed, unlocked, playUnlock, playComplete);

        String labelStyle = skin.has("medium_outline", Label.LabelStyle.class) ? "medium_outline" : "medium";
        Label number = new Label(String.valueOf(level.getIndex()), skin, labelStyle);
        number.setAlignment(Align.center);
        number.setFontScale(0.7f);

        Table overlay = new Table();
        overlay.top();
        overlay.add(number).padTop(2).row();
        overlay.add().expand();

        Stack stack = new Stack();
        stack.setSize(NODE, NODE);
        if (playback != null) {
            PamActor node = new PamActor(assets.pamPlayer());
            node.setSize(NODE, NODE);
            node.setAnchor(0.5f, 0.5f);
            node.setTouchable(Touchable.disabled);
            if (playback.thenClip() != null) {
                node.playThen(playback.pam(), playback.clip(), NODE_SCALE, playback.thenClip(), true, () -> {
                    if (playComplete && seenCompleted != null) {
                        seenCompleted.add(level.getIndex());
                    }
                    if (seenUnlocked != null) {
                        seenUnlocked.add(level.getIndex());
                    }
                });
            } else {
                node.setClip(playback.pam(), playback.clip(), NODE_SCALE, playback.loop());
            }
            stack.add(node);
        }
        stack.add(overlay);
        if (!unlocked) {
            Image lock = new Image(new TextureRegionDrawable(assets.region(AdventureAssetIds.LOCK)));
            lock.setScaling(Scaling.fit);
            Table lockPad = new Table();
            lockPad.add(lock).size(LOCK_SIZE, LOCK_SIZE);
            stack.add(lockPad);
        }

        ActorNode wrapper = new ActorNode(stack, layout);
        if (boss) {
            wrapper.setSize(layout.islandW, layout.islandH);
            wrapper.setPosition(layout.islandX, layout.islandY);
            stack.setSize(NODE, NODE);
            stack.setPosition(layout.cx - layout.islandX - NODE / 2f, layout.cy - layout.islandY - NODE / 2f);
        } else {
            wrapper.setSize(NODE, NODE);
            wrapper.setPosition(layout.cx - NODE / 2f, layout.cy - NODE / 2f);
        }
        PvzButtons.animate(wrapper, current ? 1.1f : 1.06f, 0.92f, () -> {
            if (onLevel != null) {
                onLevel.accept(level.getIndex());
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
        private final boolean boss;

        private SlotLayout(String islandId,
                           float cx,
                           float cy,
                           float islandW,
                           float islandH,
                           float islandX,
                           float islandY,
                           boolean boss) {
            this.islandId = islandId;
            this.cx = cx;
            this.cy = cy;
            this.islandW = islandW;
            this.islandH = islandH;
            this.islandX = islandX;
            this.islandY = islandY;
            this.boss = boss;
        }
    }

    private static final class ActorNode extends Group {
        private ActorNode(Stack stack, SlotLayout layout) {
            addActor(stack);
            if (!layout.boss) {
                stack.setFillParent(true);
            }
            setTransform(true);
        }
    }
}
