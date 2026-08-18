package io.github.finalwave.view.gui.hud;

import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import io.github.finalwave.model.adventure.ChapterConfig;
import io.github.finalwave.model.adventure.LevelConfig;
import io.github.finalwave.model.adventure.LevelType;
import io.github.finalwave.view.gui.widget.PanelLabels;


public final class LevelObjectiveBanner extends Table {
    private static final float WIDTH = 760f;

    private final Label title;
    private final Label body;
    private boolean shown;

    public LevelObjectiveBanner(Skin skin) {
        setFillParent(true);
        top();
        padTop(110f);
        setVisible(false);
        title = PanelLabels.title(skin, "");
        title.setFontScale(0.55f);
        title.setAlignment(Align.center);
        body = PanelLabels.body(skin, "");
        body.setAlignment(Align.center);
        Table panel = new Table(skin);
        if (skin.has("bundle_reward_multiplier", Label.LabelStyle.class)
                && skin.get("bundle_reward_multiplier", Label.LabelStyle.class).background != null) {
            panel.setBackground(skin.get("bundle_reward_multiplier", Label.LabelStyle.class).background);
        }
        panel.pad(18f, 28f, 18f, 28f);
        panel.add(title).width(WIDTH - 56f).padBottom(8f).row();
        panel.add(body).width(WIDTH - 56f);
        add(panel).width(WIDTH);
    }

    public void showOnce(ChapterConfig chapter, LevelConfig level) {
        if (shown || level == null) {
            return;
        }
        shown = true;
        title.setText(heading(chapter, level));
        body.setText(objective(level));
        setVisible(true);
        clearActions();
        getColor().a = 0f;
        addAction(Actions.sequence(
                Actions.fadeIn(0.2f),
                Actions.delay(4.2f),
                Actions.fadeOut(0.35f),
                Actions.run(() -> setVisible(false))
        ));
    }

    public void reset() {
        shown = false;
        setVisible(false);
        clearActions();
    }

    private static String heading(ChapterConfig chapter, LevelConfig level) {
        String chapterName = chapter == null || chapter.getDisplayName() == null
                ? "Adventure"
                : chapter.getDisplayName();
        return chapterName + " — Level " + level.getIndex();
    }

    private static String objective(LevelConfig level) {
        LevelType type = level == null ? null : level.getType();
        if (type == null) {
            return "Survive the zombie attack.";
        }
        return switch (type) {
            case NORMAL -> "Survive the zombie attack.";
            case CONVEYOR_BELT -> "Plant what arrives on the conveyor.";
            case LOCKED_PLANTS -> "Some plants are locked this level.";
            case SAVE_OUR_SEEDS -> "Protect the marked plants.";
            case TIMED_WAR -> "timed-sun".equals(level.getSpecialHandlerKey())
                    ? "Produce the required sun before time runs out."
                    : "Defeat the zombies before time runs out.";
            case NIGHT_OPS -> "No sky sun. Plan your producers.";
            case DEAD_LINE -> "Do not let zombies cross the line.";
            case LOVE_YOUR_PLANTS -> "Do not lose too many plants.";
            case PLANT_WHAT_YOU_GET -> "Plant freely with the starting sun, then start the waves.";
            case BOSS -> "Defeat the boss.";
        };
    }
}
