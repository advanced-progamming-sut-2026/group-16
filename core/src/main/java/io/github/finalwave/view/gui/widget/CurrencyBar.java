package io.github.finalwave.view.gui.widget;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import io.github.finalwave.model.user.User;
import io.github.finalwave.model.user.UserDatabase;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.assets.MenuAssetIds;

import java.util.function.IntSupplier;


public final class CurrencyBar extends Table {
    private static final float CHIP_HEIGHT = 56f;
    private static final float VALUE_PAD_LEFT = 72f;
    private static final float SPROUT_VALUE_PAD_LEFT = 84f;

    private static final int COIN_CHEAT_AMOUNT = 100;
    private static final int DIAMOND_CHEAT_AMOUNT = 10;

    private final GameAssets assets;
    private final Skin skin;
    private final Stack gemChip;
    private final Stack coinChip;
    private final Label coinsLabel;
    private final Label gemsLabel;
    private Stack sproutChip;
    private Label sproutsLabel;
    private IntSupplier sproutCount;
    private Runnable sproutPlus;
    private boolean sproutsEnabled;
    private User user;

    public CurrencyBar(GameAssets assets, Skin skin) {
        this.assets = assets;
        this.skin = skin;
        gemChip = chip(assets, skin, MenuAssetIds.GEM_ICON, VALUE_PAD_LEFT, this::cheatDiamonds);
        coinChip = chip(assets, skin, MenuAssetIds.COIN_ICON, VALUE_PAD_LEFT, this::cheatCoins);
        gemsLabel = (Label) gemChip.getUserObject();
        coinsLabel = (Label) coinChip.getUserObject();
        layoutChips();
    }

    public void enableSprouts(IntSupplier count, Runnable onPlus) {
        this.sproutCount = count;
        this.sproutPlus = onPlus;
        this.sproutsEnabled = true;
        if (sproutChip == null) {
            sproutChip = chip(assets, skin, MenuAssetIds.SPROUT_ICON, SPROUT_VALUE_PAD_LEFT, this::onSproutPlus);
            sproutsLabel = (Label) sproutChip.getUserObject();
        }
        layoutChips();
    }

    private void layoutChips() {
        clearChildren();
        defaults().padLeft(12);
        if (sproutsEnabled && sproutChip != null) {
            float gemWidth = widthFor(MenuAssetIds.GEM_ICON, CHIP_HEIGHT);
            TextureRegion sprout = assets.region(MenuAssetIds.SPROUT_ICON);
            float sproutHeight = gemWidth * sprout.getRegionHeight()
                    / (float) Math.max(1, sprout.getRegionWidth());
            add(sproutChip).size(gemWidth, sproutHeight);
        }
        add(gemChip).size(widthFor(MenuAssetIds.GEM_ICON, CHIP_HEIGHT), CHIP_HEIGHT);
        add(coinChip).size(widthFor(MenuAssetIds.COIN_ICON, CHIP_HEIGHT), CHIP_HEIGHT);
    }

    private float widthFor(String imageId, float height) {
        TextureRegion region = assets.region(imageId);
        return height * region.getRegionWidth() / (float) Math.max(1, region.getRegionHeight());
    }

    private static Stack chip(GameAssets assets, Skin skin, String backgroundId, float valuePadLeft, Runnable onPlusClick) {
        Image background = new Image(new TextureRegionDrawable(assets.region(backgroundId)));
        background.setScaling(Scaling.fit);

        String styleName = skin.has("medium_outline", Label.LabelStyle.class) ? "medium_outline" : "medium";
        Label value = new Label("0", skin, styleName);
        value.setAlignment(Align.left);
        value.setColor(Color.WHITE);

        Container<Label> labelContainer = new Container<>(value);
        labelContainer.left().padLeft(valuePadLeft).padRight(10f);

        Stack stack = new Stack();
        stack.setTouchable(Touchable.enabled);
        stack.add(background);
        stack.add(labelContainer);
        stack.setUserObject(value);
        stack.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {

                if (x < stack.getWidth() * 0.62f) {
                    return;
                }
                if (onPlusClick != null) {
                    onPlusClick.run();
                }
            }
        });
        return stack;
    }

    public void bind(User user) {
        this.user = user;
        refresh();
    }

    public void refresh() {
        if (sproutsEnabled && sproutsLabel != null) {
            sproutsLabel.setText(sproutCount == null ? "0" : String.valueOf(sproutCount.getAsInt()));
        }
        if (user == null) {
            gemsLabel.setText("0");
            coinsLabel.setText("0");
            return;
        }
        gemsLabel.setText(String.valueOf(user.getDiamonds()));
        coinsLabel.setText(String.valueOf(user.getCoins()));
    }

    private void onSproutPlus() {
        if (sproutPlus != null) {
            sproutPlus.run();
        }
        refresh();
    }

    private void cheatCoins() {
        if (user == null || !user.isDebugMode()) {
            return;
        }
        user.addCoins(COIN_CHEAT_AMOUNT);
        UserDatabase.getInstance().saveUserWallet(user);
        refresh();
    }

    private void cheatDiamonds() {
        if (user == null || !user.isDebugMode()) {
            return;
        }
        user.addDiamonds(DIAMOND_CHEAT_AMOUNT);
        UserDatabase.getInstance().saveUserWallet(user);
        refresh();
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        refresh();
    }
}
