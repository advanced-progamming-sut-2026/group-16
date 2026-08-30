package io.github.finalwave.view.gui.widget;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
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
import io.github.finalwave.debug.DebugCheatPersistence;
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
    private static final float COUNT_PER_SECOND = 90f;
    private static final float CATCH_UP = 8f;

    private final GameAssets assets;
    private final Skin skin;
    private final Stack gemChip;
    private final Stack coinChip;
    private final Label coinsLabel;
    private final Label gemsLabel;
    private final RollingAmount coins = new RollingAmount();
    private final RollingAmount gems = new RollingAmount();
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
        stack.setTransform(true);
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
            coins.reset();
            gems.reset();
            gemsLabel.setText("0");
            coinsLabel.setText("0");
            return;
        }
        if (gems.set(user.getDiamonds())) {
            pulse(gemChip);
        }
        if (coins.set(user.getCoins())) {
            pulse(coinChip);
        }
        writeShown();
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
        DebugCheatPersistence.addCoins(user, UserDatabase.getInstance(), COIN_CHEAT_AMOUNT);
        refresh();
    }

    private void cheatDiamonds() {
        if (user == null || !user.isDebugMode()) {
            return;
        }
        DebugCheatPersistence.addDiamonds(user, UserDatabase.getInstance(), DIAMOND_CHEAT_AMOUNT);
        refresh();
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        refresh();
        boolean coinsMoved = coins.tick(delta);
        boolean gemsMoved = gems.tick(delta);
        if (coinsMoved || gemsMoved) {
            writeShown();
        }
    }

    private void writeShown() {
        coinsLabel.setText(String.valueOf(coins.display()));
        gemsLabel.setText(String.valueOf(gems.display()));
    }

    private static void pulse(Stack chip) {
        chip.clearActions();
        chip.setOrigin(Align.center);
        chip.setScale(1f);
        chip.addAction(Actions.sequence(
                Actions.scaleTo(1.16f, 1.16f, 0.08f, Interpolation.sineOut),
                Actions.scaleTo(1f, 1f, 0.14f, Interpolation.sine)));
    }

    private static final class RollingAmount {
        private float shown;
        private int target;
        private boolean seeded;

        private boolean set(int amount) {
            int next = Math.max(0, amount);
            boolean rose = seeded && next > target;
            target = next;
            if (!seeded) {
                shown = next;
                seeded = true;
                return false;
            }
            if (target < shown) {
                shown = target;
            }
            return rose;
        }

        private boolean tick(float delta) {
            if (shown >= target) {
                return false;
            }
            float gap = target - shown;
            float step = Math.max(COUNT_PER_SECOND, gap * CATCH_UP) * delta;
            shown = Math.min(target, shown + step);
            return true;
        }

        private int display() {
            return Math.round(shown);
        }

        private void reset() {
            shown = 0;
            target = 0;
            seeded = false;
        }
    }
}
