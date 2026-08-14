package io.github.finalwave.view.gui.widget;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import io.github.finalwave.model.adventure.ChapterConfig;
import io.github.finalwave.model.user.ChapterProgress;
import io.github.finalwave.model.user.User;
import io.github.finalwave.view.gui.assets.AdventureAssetIds;
import io.github.finalwave.view.gui.assets.GameAssets;

import java.util.List;
import java.util.function.IntConsumer;

public final class ChapterCarousel extends Table {
    private static final float CENTER_WIDTH = 560f;
    private static final float CENTER_HEIGHT = 420f;
    private static final float SIDE_WIDTH = 280f;
    private static final float SIDE_HEIGHT = 210f;
    private static final float ARROW_SIZE = 96f;

    public ChapterCarousel(GameAssets assets,
                           Skin skin,
                           List<ChapterConfig> chapters,
                           int focusIndex,
                           User user,
                           IntConsumer onFocus,
                           Runnable onPlay) {
        this(assets, skin, chapters, focusIndex, user, onFocus, onPlay, null);
    }

    public ChapterCarousel(GameAssets assets,
                           Skin skin,
                           List<ChapterConfig> chapters,
                           int focusIndex,
                           User user,
                           IntConsumer onFocus,
                           Runnable onPlay,
                           Runnable onLockedPlay) {
        defaults().center();
        if (chapters == null || chapters.isEmpty()) {
            return;
        }
        int focus = Math.max(0, Math.min(focusIndex, chapters.size() - 1));
        ChapterConfig current = chapters.get(focus);
        ChapterProgress progress = user == null ? null : user.getChapterProgress();
        boolean debug = user != null && user.isDebugMode();
        boolean unlocked = debug || (progress != null && progress.isChapterUnlocked(current.getId()));
        int completed = progress == null ? 0 : progress.getCompletedLevels(current.getId()).size();
        int total = current.getLevels().size();

        Table worlds = new Table();
        if (focus > 0) {
            worlds.add(worldStack(assets, chapters.get(focus - 1), SIDE_WIDTH, SIDE_HEIGHT, 0.55f, false))
                    .size(SIDE_WIDTH, SIDE_HEIGHT).padRight(24);
        } else {
            worlds.add().size(SIDE_WIDTH, SIDE_HEIGHT).padRight(24);
        }

        Actor leftArrow = PvzButtons.iconButton(assets.region(AdventureAssetIds.ARROW_LEFT), ARROW_SIZE, ARROW_SIZE,
                () -> {
                    if (focus > 0 && onFocus != null) {
                        onFocus.accept(focus - 1);
                    }
                });
        leftArrow.setVisible(focus > 0);
        worlds.add(leftArrow).size(ARROW_SIZE).padRight(12);

        worlds.add(worldStack(assets, current, CENTER_WIDTH, CENTER_HEIGHT, 1f, !unlocked))
                .size(CENTER_WIDTH, CENTER_HEIGHT);

        Actor rightArrow = PvzButtons.iconButton(assets.region(AdventureAssetIds.ARROW_RIGHT), ARROW_SIZE, ARROW_SIZE,
                () -> {
                    if (focus < chapters.size() - 1 && onFocus != null) {
                        onFocus.accept(focus + 1);
                    }
                });
        rightArrow.setVisible(focus < chapters.size() - 1);
        worlds.add(rightArrow).size(ARROW_SIZE).padLeft(12);

        if (focus < chapters.size() - 1) {
            worlds.add(worldStack(assets, chapters.get(focus + 1), SIDE_WIDTH, SIDE_HEIGHT, 0.55f, false))
                    .size(SIDE_WIDTH, SIDE_HEIGHT).padLeft(24);
        } else {
            worlds.add().size(SIDE_WIDTH, SIDE_HEIGHT).padLeft(24);
        }

        add(worlds).padBottom(18).row();

        Label title = new Label(current.getDisplayName(), skin, "big_outline");
        title.setAlignment(Align.center);
        add(title).padBottom(8).row();

        String badgeStyle = skin.has("medium_outline", Label.LabelStyle.class) ? "medium_outline" : "medium";
        Label progressLabel = new Label(completed + "/" + total, skin, badgeStyle);
        progressLabel.setAlignment(Align.center);
        add(progressLabel).padBottom(16).row();

        TextButton play = PvzButtons.textButton(unlocked ? "PLAY" : "LOCKED", skin, "purple", () -> {
            if (unlocked) {
                if (onPlay != null) {
                    onPlay.run();
                }
            } else if (onLockedPlay != null) {
                onLockedPlay.run();
            }
        });
        add(play).width(240).height(72).padBottom(18).row();

        Table dots = new Table();
        for (int i = 0; i < chapters.size(); i++) {
            Image dot = navDot(skin, i == focus);
            int index = i;
            Stack hit = new Stack();
            hit.add(dot);
            hit.setSize(22, 22);
            PvzButtons.animate(hit, 1.15f, 0.9f, () -> {
                if (onFocus != null) {
                    onFocus.accept(index);
                }
            });
            dots.add(hit).size(22).pad(6);
        }
        add(dots);
    }

    private static Stack worldStack(GameAssets assets,
                                    ChapterConfig chapter,
                                    float width,
                                    float height,
                                    float alpha,
                                    boolean locked) {
        Image world = new Image(new TextureRegionDrawable(assets.region(AdventureAssetIds.worldIcon(chapter.getId()))));
        world.setScaling(Scaling.fit);
        world.setColor(1f, 1f, 1f, alpha);
        Stack stack = new Stack();
        stack.setSize(width, height);
        stack.add(world);
        if (locked) {
            Image lock = new Image(new TextureRegionDrawable(assets.region(AdventureAssetIds.LOCK)));
            lock.setScaling(Scaling.fit);
            Table lockTable = new Table();
            lockTable.add(lock).size(width * 0.28f, height * 0.28f);
            stack.add(lockTable);
        }
        return stack;
    }

    private static Image navDot(Skin skin, boolean selected) {
        TextureRegion region = findRegion(skin, selected
                ? "image_ui_generic_navdot_fill"
                : "image_ui_generic_navdot");
        Image image;
        if (region != null) {
            image = new Image(new TextureRegionDrawable(region));
        } else {
            image = new Image();
            image.setColor(selected ? Color.WHITE : Color.GRAY);
        }
        image.setScaling(Scaling.fit);
        image.setTouchable(Touchable.disabled);
        return image;
    }

    private static TextureRegion findRegion(Skin skin, String name) {
        if (skin.has(name, TextureRegion.class)) {
            return skin.getRegion(name);
        }
        if (skin.getAtlas() != null) {
            return skin.getAtlas().findRegion(name);
        }
        return null;
    }
}
