package io.github.finalwave.view.gui.widget;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import io.github.finalwave.model.quest.Quest;
import io.github.finalwave.model.quest.reward.QuestReward;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.assets.MenuAssetIds;

public final class QuestRow extends Table {
    public static final float ROW_HEIGHT = 132f;

    private static final float ICON_SIZE = 82f;
    private static final float REWARD_ICON = 50f;
    private static final float BUTTON_WIDTH = 142f;
    private static final float BUTTON_HEIGHT = 54f;
    private static final float BAR_HEIGHT = 22f;

    private final GameAssets assets;
    private final Skin skin;

    public QuestRow(GameAssets assets, Skin skin, Quest quest, Runnable onClaim) {
        this.assets = assets;
        this.skin = skin;
        setTouchable(Touchable.childrenOnly);

        Stack card = new Stack();
        Image background = new Image(new TextureRegionDrawable(
                assets.region(MenuAssetIds.QUEST_CARD_BACKGROUND)));
        background.setScaling(Scaling.stretch);
        background.setTouchable(Touchable.disabled);
        card.add(background);

        Table content = new Table();
        content.pad(12f, 20f, 12f, 20f);
        content.add(questIcon(quest)).size(ICON_SIZE).padRight(18f);
        content.add(details(quest)).growX();
        content.add(actions(quest, onClaim)).right().padLeft(18f);
        card.add(content);
        add(card).grow();

        if (isClaimable(quest)) {
            addActor(badge());
        }
    }

    private Actor questIcon(Quest quest) {
        Stack stack = new Stack();
        if (quest.isCompleted()) {
            Image complete = new Image(new TextureRegionDrawable(
                    assets.region(MenuAssetIds.QUEST_COMPLETE_FRAME)));
            complete.setScaling(Scaling.fit);
            complete.setTouchable(Touchable.disabled);
            stack.add(complete);
        }
        Image icon = new Image(new TextureRegionDrawable(assets.region(iconId(quest))));
        icon.setScaling(Scaling.fit);
        Table host = new Table();
        host.add(icon).size(ICON_SIZE - 16f);
        stack.add(host);
        return stack;
    }

    private Table details(Quest quest) {
        Table details = new Table();
        details.left();

        Label title = styledLabel(quest.getTitle(), "FBUSV8C5EI_1", Color.BROWN, 1f);
        title.setWrap(true);
        details.add(title).growX().left().row();

        Label description = styledLabel(
                quest.getDescription(),
                "FBUSV8C5EI_2",
                Color.DARK_GRAY,
                0.82f);
        description.setWrap(true);
        details.add(description).growX().left().padTop(2f).row();

        details.add(progressBar(QuestProgressDisplay.of(quest)))
                .growX()
                .height(BAR_HEIGHT)
                .padTop(6f);
        return details;
    }

    private Actor progressBar(QuestProgressDisplay.Amount amount) {
        Stack stack = new Stack();
        int target = Math.max(1, amount.target());
        ProgressBar progress = new ProgressBar(0f, target, 1f, false, skin, "xp_green");
        progress.setValue(Math.min(target, amount.current()));
        progress.setAnimateDuration(0.2f);
        stack.add(progress);
        Label label = outlineLabel(amount.label(), 0.7f);
        label.setAlignment(Align.center);
        Table host = new Table();
        host.add(label);
        stack.add(host);
        return stack;
    }

    private Table actions(Quest quest, Runnable onClaim) {
        Table actions = new Table();
        actions.add(rewardLine(quest.getReward())).padRight(16f);
        actions.add(actionButton(quest, onClaim)).size(BUTTON_WIDTH, BUTTON_HEIGHT);
        return actions;
    }

    private Table rewardLine(QuestReward reward) {
        Table line = new Table();
        Image icon = new Image(new TextureRegionDrawable(rewardIcon(reward)));
        icon.setScaling(Scaling.fit);
        line.add(icon).size(REWARD_ICON).padRight(6f);
        Label amount = outlineLabel(rewardLabel(reward), 1f);
        line.add(amount).left();
        return line;
    }

    private Actor actionButton(Quest quest, Runnable onClaim) {
        boolean claimable = isClaimable(quest);
        String text = claimable ? "CLAIM" : quest.isRewardClaimed() ? "CLAIMED" : "OPEN";
        String style = skin.has("green_small", TextButton.TextButtonStyle.class) ? "green_small" : "brown";
        TextButton button = PvzButtons.textButton(text, skin, style, claimable ? onClaim : null);
        button.setDisabled(!claimable);
        button.setTouchable(claimable ? Touchable.enabled : Touchable.disabled);
        return button;
    }

    private Actor badge() {
        Table badge = new Table();
        badge.setBackground(TravelLogChrome.badge());
        Label mark = new Label("!", skin, "medium");
        mark.setColor(Color.WHITE);
        mark.setAlignment(Align.center);
        mark.setFontScale(0.78f);
        badge.add(mark).size(22f);
        badge.setSize(28f, 28f);
        badge.setPosition(-6f, ROW_HEIGHT - 24f);
        badge.setTouchable(Touchable.disabled);
        return badge;
    }

    private Label styledLabel(String text, String fontName, Color color, float scale) {
        Label label;
        if (skin.has(fontName, BitmapFont.class)) {
            label = new Label(text, skin, fontName, color);
        } else {
            label = new Label(text, skin, "medium");
            label.setColor(color);
        }
        label.setFontScale(scale);
        return label;
    }

    private Label outlineLabel(String text, float scale) {
        String style = skin.has("medium_outline", Label.LabelStyle.class) ? "medium_outline" : "medium";
        Label label = new Label(text, skin, style);
        label.setFontScale(scale);
        return label;
    }

    private TextureRegion rewardIcon(QuestReward reward) {
        if (reward.getDiamonds() > 0) {
            return assets.region(MenuAssetIds.QUEST_REWARD_GEMS);
        }
        if (reward.getUnlockTargetId() != null || reward.getSeedPacketCount() > 0) {
            return assets.region(MenuAssetIds.SEED_PACKET_MULTI);
        }
        return assets.region(MenuAssetIds.QUEST_REWARD_COINS);
    }

    private static String iconId(Quest quest) {
        return switch (quest.getCategory()) {
            case DAILY -> MenuAssetIds.QUEST_ICON_DAILY;
            case MAIN -> MenuAssetIds.QUEST_ICON_MAIN;
            case EPIC_CHALLENGE -> MenuAssetIds.QUEST_ICON_EPIC;
        };
    }

    private static boolean isClaimable(Quest quest) {
        return quest.isCompleted() && !quest.isRewardClaimed();
    }

    private static String rewardLabel(QuestReward reward) {
        if (reward.getCoins() > 0) {
            return "X" + reward.getCoins();
        }
        if (reward.getDiamonds() > 0) {
            return "X" + reward.getDiamonds();
        }
        if (reward.getSeedPacketCount() > 0) {
            return "X" + reward.getSeedPacketCount();
        }
        if ("RANDOM_PLANT".equals(reward.getUnlockTargetId())) {
            return "Random plant";
        }
        if (reward.getUnlockTargetId() != null) {
            return reward.getUnlockTargetId();
        }
        return reward.describe();
    }
}
