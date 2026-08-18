package io.github.finalwave.view.gui.hud.special;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.game.MatchResult;
import io.github.finalwave.model.game.TimedWarMode;
import io.github.finalwave.model.game.TimedWarRules;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.assets.LawnAssetIds;


public final class TimedWarPanel extends Table {
    private static final float ICON_SIZE = 64f;
    private static final float BANNER_HEIGHT = 44f;
    private static final float CHIP_HEIGHT = 68f;
    private static final float GOAL_WIDTH = 196f;
    private static final float TIMER_WIDTH = 168f;
    private static final float GAP = 10f;
    private static final float BANNER_INSET = 30f;
    private static final float TEXT_PAD_LEFT = 82f;
    private static final float TEXT_SCALE = 0.7f;

    private final GoalChip goal;
    private final TimerChip timer;
    private boolean shown;

    public TimedWarPanel(GameAssets assets) {
        goal = new GoalChip(assets);
        timer = new TimerChip(assets);
        add(goal).size(GOAL_WIDTH, CHIP_HEIGHT).padRight(GAP);
        add(timer).size(TIMER_WIDTH, CHIP_HEIGHT);
        setVisible(false);
        shown = false;
        setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
    }

    @Override
    public float getPrefWidth() {
        return isVisible() ? GOAL_WIDTH + GAP + TIMER_WIDTH : 0f;
    }

    @Override
    public float getPrefHeight() {
        return isVisible() ? CHIP_HEIGHT : 0f;
    }

    public void refresh(GameSession session) {
        TimedWarRules rules = session == null ? null : session.getTimedWarRules();
        boolean active = session != null
                && session.isTimedWarActive()
                && rules != null
                && rules.isActiveRules();
        if (active != shown) {
            shown = active;
            setVisible(active);
            invalidateHierarchy();
        }
        if (!active) {
            return;
        }
        int goalAmount = Math.max(1, rules.getGoalAmount());
        int progress = Math.max(0, Math.min(goalAmount, session.getTimedWarProgress()));
        int remaining = Math.max(0, goalAmount - progress);
        int remainingSeconds = Math.max(0, session.getTimedWarRemainingTicks() / GameSession.TICKS_PER_SECOND);
        int durationSeconds = Math.max(1, rules.getDurationSeconds());
        boolean won = session.isTimedWarGoalMet() || session.getMatchResult() == MatchResult.WON;
        boolean lost = session.getMatchResult() == MatchResult.LOST;
        goal.refresh(rules.getMode(), progress / (float) goalAmount, remaining, won, lost);
        timer.refresh(remainingSeconds, remainingSeconds / (float) durationSeconds);
    }

    private static final class GoalChip extends WidgetGroup {
        private final Image banner;
        private final Image fill;
        private final Image icon;
        private final Image status;
        private final Label value;
        private final TextureRegionDrawable killIcon;
        private final TextureRegionDrawable sunIcon;
        private final TextureRegionDrawable success;
        private final TextureRegionDrawable failed;
        private float progress;

        private GoalChip(GameAssets assets) {
            setTransform(false);
            banner = new Image(new NinePatchDrawable(new NinePatch(
                    assets.region(LawnAssetIds.SUN_BANNER), 16, 16, 8, 8)));
            banner.setScaling(Scaling.stretch);
            fill = new Image(new TextureRegionDrawable(assets.region(LawnAssetIds.PROGRESS_FILL)));
            fill.setScaling(Scaling.stretch);
            killIcon = new TextureRegionDrawable(assets.region(LawnAssetIds.CHALLENGE_ZOMBIE_HEAD));
            sunIcon = new TextureRegionDrawable(assets.region(LawnAssetIds.CHALLENGE_SUN_PRODUCED));
            success = new TextureRegionDrawable(assets.region(LawnAssetIds.CHALLENGE_SUCCESS));
            failed = new TextureRegionDrawable(assets.region(LawnAssetIds.CHALLENGE_FAILED));
            icon = new Image(killIcon);
            icon.setScaling(Scaling.fit);
            status = new Image(success);
            status.setScaling(Scaling.fit);
            status.setVisible(false);
            value = new Label("0 Left", assets.skin(), outlineStyle(assets));
            value.setAlignment(Align.left);
            value.setFontScale(TEXT_SCALE);
            value.setColor(Color.WHITE);
            addActor(banner);
            addActor(fill);
            addActor(value);
            addActor(icon);
            addActor(status);
            setSize(GOAL_WIDTH, CHIP_HEIGHT);
        }

        void refresh(TimedWarMode mode, float progress, int remaining, boolean won, boolean lost) {
            icon.setDrawable(mode == TimedWarMode.SUN ? sunIcon : killIcon);
            if (won) {
                value.setText("Done");
                status.setDrawable(success);
                status.setVisible(true);
            } else if (lost) {
                value.setText("Fail");
                status.setDrawable(failed);
                status.setVisible(true);
            } else {
                value.setText(remaining + " Left");
                status.setVisible(false);
            }
            this.progress = Math.max(0f, Math.min(1f, progress));
            layoutFill(this.progress);
        }

        @Override
        public void layout() {
            float height = getHeight();
            float bannerY = (height - BANNER_HEIGHT) * 0.5f;
            banner.setBounds(BANNER_INSET, bannerY, getWidth() - BANNER_INSET, BANNER_HEIGHT);
            value.setBounds(TEXT_PAD_LEFT, bannerY, getWidth() - TEXT_PAD_LEFT - 10f, BANNER_HEIGHT);
            icon.setBounds(0f, (height - ICON_SIZE) * 0.5f, ICON_SIZE, ICON_SIZE);
            status.setBounds(ICON_SIZE - 22f, (height - 28f) * 0.5f + 10f, 28f, 28f);
            layoutFill(progress);
            icon.toFront();
            status.toFront();
        }

        private void layoutFill(float progress) {
            float bannerY = (getHeight() - BANNER_HEIGHT) * 0.5f;
            float maxWidth = Math.max(1f, banner.getWidth()) * 0.92f;
            float fillWidth = maxWidth * progress;
            fill.setVisible(fillWidth > 1f);
            fill.setSize(fillWidth, BANNER_HEIGHT * 0.62f);
            fill.setPosition(banner.getX() + 6f, bannerY + (BANNER_HEIGHT - fill.getHeight()) * 0.5f);
        }
    }

    private static final class TimerChip extends WidgetGroup {
        private final Image banner;
        private final Image icon;
        private final Label value;

        private TimerChip(GameAssets assets) {
            setTransform(false);
            banner = new Image(new NinePatchDrawable(new NinePatch(
                    assets.region(LawnAssetIds.SUN_BANNER), 16, 16, 8, 8)));
            banner.setScaling(Scaling.stretch);
            icon = new Image(new TextureRegionDrawable(assets.region(LawnAssetIds.CHALLENGE_TIMER_ICON)));
            icon.setScaling(Scaling.fit);
            value = new Label("0:00", assets.skin(), outlineStyle(assets));
            value.setAlignment(Align.left);
            value.setFontScale(TEXT_SCALE);
            value.setColor(Color.WHITE);
            addActor(banner);
            addActor(value);
            addActor(icon);
            setSize(TIMER_WIDTH, CHIP_HEIGHT);
        }

        void refresh(int remainingSeconds, float remainingFraction) {
            int minutes = remainingSeconds / 60;
            int seconds = remainingSeconds % 60;
            value.setText(minutes + ":" + (seconds < 10 ? "0" : "") + seconds);
            if (remainingFraction < 0.25f) {
                value.setColor(1f, 0.72f, 0.38f, 1f);
            } else {
                value.setColor(Color.WHITE);
            }
        }

        @Override
        public void layout() {
            float height = getHeight();
            float bannerY = (height - BANNER_HEIGHT) * 0.5f;
            banner.setBounds(BANNER_INSET, bannerY, getWidth() - BANNER_INSET, BANNER_HEIGHT);
            value.setBounds(TEXT_PAD_LEFT, bannerY, getWidth() - TEXT_PAD_LEFT - 10f, BANNER_HEIGHT);
            icon.setBounds(0f, (height - ICON_SIZE) * 0.5f, ICON_SIZE, ICON_SIZE);
            icon.toFront();
        }
    }

    private static String outlineStyle(GameAssets assets) {
        if (assets.skin().has("medium_outline", Label.LabelStyle.class)) {
            return "medium_outline";
        }
        if (assets.skin().has("big_outline", Label.LabelStyle.class)) {
            return "big_outline";
        }
        return "medium";
    }
}
