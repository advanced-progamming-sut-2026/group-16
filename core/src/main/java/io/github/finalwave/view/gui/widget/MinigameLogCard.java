package io.github.finalwave.view.gui.widget;

import com.badlogic.gdx.graphics.Color;
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
import io.github.finalwave.controller.TravelLogController;
import io.github.finalwave.model.minigame.MiniGameId;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.assets.MenuAssetIds;

public final class MinigameLogCard extends Table {
    public static final float CARD_HEIGHT = 190f;

    private final Skin skin;

    public MinigameLogCard(
            GameAssets assets,
            Skin skin,
            TravelLogController.MiniGameLogEntry entry,
            Runnable onPlay) {
        this.skin = skin;
        setTouchable(Touchable.childrenOnly);
        Stack card = new Stack();
        Image background = new Image(new TextureRegionDrawable(
                assets.region(backgroundId(entry.id()))));
        background.setScaling(Scaling.stretch);
        background.setTouchable(Touchable.disabled);
        card.add(background);

        Table shade = new Table();
        shade.setBackground(TravelLogChrome.minigameCard());
        shade.setTouchable(Touchable.disabled);
        card.add(shade);

        Table content = new Table();
        content.pad(18f, 24f, 16f, 24f);
        content.top().left();

        Label title = new Label(titleFor(entry), skin, "big");
        title.setColor(TravelLogChrome.CARD_TITLE);
        title.setWrap(true);
        title.setFontScale(0.74f);
        content.add(title).growX().left().row();

        Label body = new Label(bodyText(entry), skin, "medium");
        body.setColor(Color.WHITE);
        body.setWrap(true);
        body.setFontScale(0.76f);
        content.add(body).grow().left().top().padTop(6f).row();

        Table footer = new Table();
        footer.left().bottom();
        footer.add(rewardLabel(entry)).left().expandX();

        Table actions = new Table();
        Label status = new Label(statusText(entry), skin, "medium");
        status.setColor(TravelLogChrome.TIMER_YELLOW);
        status.setAlignment(Align.right);
        status.setFontScale(0.68f);
        actions.add(status).right().padBottom(5f).row();
        actions.add(playButton(onPlay)).size(146f, 50f).right();
        footer.add(actions).right();
        content.add(footer).growX().bottom().padTop(8f);
        card.add(content);
        add(card).grow();
    }

    private Actor rewardLabel(TravelLogController.MiniGameLogEntry entry) {
        Table strip = new Table();
        strip.setBackground(TravelLogChrome.rewardStrip());
        Label reward = new Label("Rewards: " + entry.rewardPts() + " pts", skin, "medium");
        reward.setColor(Color.WHITE);
        reward.setFontScale(0.68f);
        strip.add(reward).pad(6f, 12f, 6f, 12f);
        return strip;
    }

    private Actor playButton(Runnable onPlay) {
        String style = skin.has("purple", TextButton.TextButtonStyle.class) ? "purple" : "brown";
        return PvzButtons.textButton("PLAY", skin, style, onPlay);
    }

    private static String titleFor(TravelLogController.MiniGameLogEntry entry) {
        return entry.locked() ? "Unlock " + entry.displayName() + "!" : entry.displayName();
    }

    private static String bodyText(TravelLogController.MiniGameLogEntry entry) {
        if (entry.locked()) {
            return entry.description() + " Unlock this minigame to access its levels.";
        }
        if (!entry.implemented()) {
            return entry.description() + " Coming soon.";
        }
        return entry.description();
    }

    private static String statusText(TravelLogController.MiniGameLogEntry entry) {
        if (entry.locked()) {
            return "Locked";
        }
        int remaining = entry.remainingStages();
        if (remaining <= 0) {
            return "All levels complete!";
        }
        return remaining == 1 ? "1 level remaining!" : remaining + " levels remaining!";
    }

    private static String backgroundId(MiniGameId id) {
        return switch (id) {
            case VASE_BREAKER -> MenuAssetIds.MINIGAME_VASEBREAKER_ART;
            case WALNUT_BOWLING -> MenuAssetIds.MINIGAME_BOWLING_ART;
            case I_ZOMBIE -> MenuAssetIds.MINIGAME_I_ZOMBIE_ART;
            case BEGHOULED -> MenuAssetIds.MINIGAME_BEGHOULED_ART;
            case ZOMBOTANY -> MenuAssetIds.MINIGAME_ZOMBOTANY_ART;
        };
    }
}
