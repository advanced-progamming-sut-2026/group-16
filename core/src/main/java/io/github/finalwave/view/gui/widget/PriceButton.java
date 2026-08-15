package io.github.finalwave.view.gui.widget;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import io.github.finalwave.model.shop.ShopOffer;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.assets.MenuAssetIds;

import java.util.Locale;


public final class PriceButton extends Stack {
    private static final float HOVER_SCALE = 1.08f;
    private static final float PRESS_SCALE = 0.94f;
    private static final float ICON_HEIGHT_SHARE = 0.90f;
    private static final float ICON_HANG = 0.18f;
    private static final Color PRICE_TEXT = Color.valueOf("FFFFFF");
    private static final Color SOLD_OUT_TEXT = Color.valueOf("EFEADF");

    private final Table frame;
    private final Drawable up;
    private final Drawable down;
    private final Runnable onBuy;
    private final boolean enabled;
    private boolean hovering;

    private PriceButton(
            Drawable up,
            Drawable down,
            Actor content,
            float width,
            float height,
            Runnable onBuy,
            boolean enabled) {
        this.up = up;
        this.down = down == null ? up : down;
        this.onBuy = onBuy;
        this.enabled = enabled;
        this.frame = new Table();
        this.frame.setBackground(up);
        this.frame.setFillParent(true);
        this.frame.setTouchable(Touchable.disabled);
        if (content instanceof Layout layout) {
            layout.setFillParent(true);
        }
        content.setTouchable(Touchable.disabled);
        add(frame);
        add(content);
        setSize(width, height);
        setTransform(true);
        setOrigin(Align.center);
        setTouchable(enabled ? Touchable.enabled : Touchable.disabled);
        if (enabled) {
            addListener(new PressListener());
        }
    }

    public static Actor of(
            GameAssets assets,
            Skin skin,
            ShopOffer offer,
            float width,
            float height,
            Runnable onBuy) {
        if (offer.soldOut()) {
            Table content = new Table();
            content.add(priceLabel(skin, "Sold out", SOLD_OUT_TEXT, 0.62f)).expand().center();
            return new PriceButton(
                    StoreChrome.disabledButton(assets.region(MenuAssetIds.STORE_BUY_DISABLED)),
                    null,
                    content,
                    width,
                    height,
                    null,
                    false);
        }
        if (offer.pricedInDiamonds()) {
            return priced(
                    assets,
                    skin,
                    MenuAssetIds.STORE_PRICE_GEM,
                    StoreChrome.gemButton(assets.region(MenuAssetIds.STORE_GEM_PLATE)),
                    StoreChrome.gemButton(assets.region(MenuAssetIds.STORE_GEM_PLATE_DOWN)),
                    offer.price(),
                    width,
                    height,
                    onBuy);
        }
        return priced(
                assets,
                skin,
                MenuAssetIds.STORE_PRICE_COIN,
                StoreChrome.coinButton(assets.region(MenuAssetIds.STORE_COIN_PLATE)),
                StoreChrome.coinButton(assets.region(MenuAssetIds.STORE_COIN_PLATE_DOWN)),
                offer.price(),
                width,
                height,
                onBuy);
    }

    public static Actor coins(
            GameAssets assets,
            Skin skin,
            int price,
            float width,
            float height,
            Runnable onBuy) {
        return priced(
                assets,
                skin,
                MenuAssetIds.STORE_PRICE_COIN,
                StoreChrome.coinButton(assets.region(MenuAssetIds.STORE_COIN_PLATE)),
                StoreChrome.coinButton(assets.region(MenuAssetIds.STORE_COIN_PLATE_DOWN)),
                price,
                width,
                height,
                onBuy);
    }

    public static Actor coinsLabeled(
            GameAssets assets,
            Skin skin,
            String caption,
            int price,
            float width,
            float height,
            Runnable onBuy) {
        return coinsLabeled(
                assets,
                skin,
                caption,
                price,
                width,
                height,
                StoreChrome.coinButton(assets.region(MenuAssetIds.STORE_COIN_PLATE)),
                StoreChrome.coinButton(assets.region(MenuAssetIds.STORE_COIN_PLATE_DOWN)),
                onBuy);
    }

    public static Actor gemsLabeled(
            GameAssets assets,
            Skin skin,
            String caption,
            int price,
            float width,
            float height,
            Runnable onBuy) {
        TextureRegion region = assets.region(MenuAssetIds.STORE_PRICE_GEM);
        float iconHeight = height * 0.62f;
        float iconWidth = iconHeight * region.getRegionWidth() / (float) Math.max(1, region.getRegionHeight());
        Image icon = new Image(new TextureRegionDrawable(region));
        icon.setScaling(Scaling.fit);
        Table content = new Table();
        content.add(priceLabel(skin, caption, PRICE_TEXT, 0.62f)).padLeft(18f).padRight(8f);
        content.add().expandX();
        content.add(icon).size(iconWidth, iconHeight).padRight(6f);
        content.add(priceLabel(skin, amount(price), PRICE_TEXT, 0.78f)).padRight(16f);
        return new PriceButton(
                StoreChrome.gemButton(assets.region(MenuAssetIds.STORE_GEM_PLATE)),
                StoreChrome.gemButton(assets.region(MenuAssetIds.STORE_GEM_PLATE_DOWN)),
                content,
                width,
                height,
                onBuy,
                true);
    }

    public static Actor coinsLabeled(
            GameAssets assets,
            Skin skin,
            String caption,
            int price,
            float width,
            float height,
            Drawable up,
            Drawable down,
            Runnable onBuy) {
        TextureRegion region = assets.region(MenuAssetIds.STORE_PRICE_COIN);
        float iconHeight = height * 0.62f;
        float iconWidth = iconHeight * region.getRegionWidth() / (float) Math.max(1, region.getRegionHeight());
        Image icon = new Image(new TextureRegionDrawable(region));
        icon.setScaling(Scaling.fit);
        Table content = new Table();
        content.add(priceLabel(skin, caption, PRICE_TEXT, 0.62f)).padLeft(18f).padRight(8f);
        content.add().expandX();
        content.add(icon).size(iconWidth, iconHeight).padRight(6f);
        content.add(priceLabel(skin, amount(price), PRICE_TEXT, 0.78f)).padRight(16f);
        return new PriceButton(up, down, content, width, height, onBuy, true);
    }

    private static PriceButton priced(
            GameAssets assets,
            Skin skin,
            String iconId,
            Drawable up,
            Drawable down,
            int price,
            float width,
            float height,
            Runnable onBuy) {
        TextureRegion region = assets.region(iconId);
        float iconHeight = height * ICON_HEIGHT_SHARE;
        float iconWidth = iconHeight * region.getRegionWidth() / (float) Math.max(1, region.getRegionHeight());
        float hang = iconWidth * ICON_HANG;
        Image icon = new Image(new TextureRegionDrawable(region));
        icon.setScaling(Scaling.fit);

        Table priceHost = new Table();
        priceHost.add(priceLabel(skin, amount(price), PRICE_TEXT, 0.82f)).expand().center();

        Table iconHost = new Table();
        iconHost.left();
        iconHost.add(icon).size(iconWidth, iconHeight).padLeft(-hang);

        Stack content = new Stack();
        content.add(priceHost);
        content.add(iconHost);
        return new PriceButton(up, down, content, width, height, onBuy, true);
    }

    public static String amount(int value) {
        return String.format(Locale.US, "%,d", value);
    }

    @Override
    protected void sizeChanged() {
        super.sizeChanged();
        setOrigin(getWidth() * 0.5f, getHeight() * 0.5f);
    }

    @Override
    public Actor hit(float x, float y, boolean touchable) {
        if (touchable && getTouchable() == Touchable.disabled) {
            return null;
        }
        if (x < 0 || x >= getWidth() || y < 0 || y >= getHeight()) {
            return null;
        }
        return this;
    }

    private void showDown(boolean pressed) {
        frame.setBackground(pressed ? down : up);
    }

    private void scaleTo(float scale, float duration) {
        clearActions();
        setOrigin(getWidth() * 0.5f, getHeight() * 0.5f);
        addAction(Actions.scaleTo(scale, scale, duration, Interpolation.sineOut));
    }

    private static Label priceLabel(Skin skin, String text, Color color, float scale) {
        String styleName = skin.has("medium_outline", Label.LabelStyle.class) ? "medium_outline" : "medium";
        Label label = new Label(text, skin, styleName);
        label.setAlignment(Align.center);
        label.setColor(color);
        label.setFontScale(scale);
        return label;
    }

    private final class PressListener extends ClickListener {
        private PressListener() {
            setTapSquareSize(48f);
        }

        @Override
        public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
            if (pointer != -1) {
                return;
            }
            hovering = true;
            if (!isPressed()) {
                scaleTo(HOVER_SCALE, 0.1f);
            }
        }

        @Override
        public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
            if (pointer != -1) {
                return;
            }
            hovering = false;
            if (!isPressed()) {
                scaleTo(1f, 0.1f);
            }
        }

        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            boolean started = super.touchDown(event, x, y, pointer, button);
            if (started) {
                showDown(true);
                scaleTo(PRESS_SCALE, 0.05f);
            }
            return started;
        }

        @Override
        public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
            super.touchUp(event, x, y, pointer, button);
            showDown(false);
            scaleTo(hovering ? HOVER_SCALE : 1f, 0.08f);
        }

        @Override
        public void clicked(InputEvent event, float x, float y) {
            if (enabled && onBuy != null) {
                onBuy.run();
            }
        }
    }
}
