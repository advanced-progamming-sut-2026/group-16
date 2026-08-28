package io.github.finalwave.view.gui.widget;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import io.github.finalwave.model.shop.ShopOffer;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.assets.LawnAssetIds;
import io.github.finalwave.view.gui.assets.MenuAssetIds;
import io.github.finalwave.view.gui.assets.PlantAnimationCatalog;
import io.github.finalwave.view.gui.assets.PlantPacketIds;


public final class ShopItemCard extends Stack {
    public static final float CARD_WIDTH = 268f;
    public static final float CARD_HEIGHT = 388f;

    private static final float TITLE_HEIGHT = 78f;
    private static final float ART_SIZE = 128f;
    private static final float PRICE_WIDTH = 180f;
    private static final float PRICE_HEIGHT = 58f;
    private static final float RIBBON_HEIGHT = 34f;
    private static final float PRICE_BOTTOM = 22f;
    private static final Color TITLE_WHITE = Color.valueOf("FFFFFF");
    private static final Color RIBBON_TEXT = Color.valueOf("FFF1D6");
    private static final Color QUANTITY_WHITE = Color.valueOf("FFFFFF");

    private final GameAssets assets;
    private final PlantAnimationCatalog catalog;
    private final Skin skin;

    public ShopItemCard(GameAssets assets, PlantAnimationCatalog catalog, Skin skin) {
        this.assets = assets;
        this.catalog = catalog;
        this.skin = skin;
        setTouchable(Touchable.childrenOnly);
    }

    public void bind(ShopOffer offer, Runnable onBuy) {
        clearChildren();
        Image chrome = new Image(StoreChrome.card(assets.region(chromeFor(offer))));
        chrome.setScaling(Scaling.stretch);
        chrome.setFillParent(true);
        chrome.setTouchable(Touchable.disabled);
        add(chrome);

        Table content = new Table();
        content.setTouchable(Touchable.childrenOnly);
        content.top();
        content.pad(14f, 16f, PRICE_HEIGHT + PRICE_BOTTOM + 16f, 16f);

        Label title = new Label(offer.name(), skin, titleStyle());
        title.setAlignment(Align.center);
        title.setWrap(true);
        title.setColor(TITLE_WHITE);
        title.setFontScale(titleScale());
        content.add(title).growX().height(TITLE_HEIGHT).padTop(6f).padLeft(10f).padRight(10f).row();

        if (hasText(offer.remainingLabel())) {
            content.add(ribbon(offer.remainingLabel())).width(CARD_WIDTH - 48f).height(RIBBON_HEIGHT).padBottom(4f).row();
        }

        content.add(preview(offer)).grow().padTop(4f).padBottom(8f);
        add(content);

        Table footer = new Table();
        footer.setTouchable(Touchable.childrenOnly);
        footer.bottom();
        footer.padBottom(PRICE_BOTTOM);
        footer.add(PriceButton.of(assets, skin, offer, PRICE_WIDTH, PRICE_HEIGHT, onBuy))
                .size(PRICE_WIDTH, PRICE_HEIGHT);
        add(footer);
    }

    private Table preview(ShopOffer offer) {
        Table host = new Table();
        host.top();
        host.add(previewActor(offer)).size(ART_SIZE).center().padTop(6f).row();
        if (hasText(offer.quantityLabel())) {
            Label quantity = new Label(offer.quantityLabel(), skin, outlineStyle());
            quantity.setAlignment(Align.center);
            quantity.setColor(QUANTITY_WHITE);
            quantity.setFontScale(0.78f);
            host.add(quantity).padTop(2f);
        }
        return host;
    }

    private Table ribbon(String text) {
        Table host = new Table();
        host.setBackground(StoreChrome.ribbon(assets.region(MenuAssetIds.STORE_RIBBON)));
        Label label = new Label(text, skin, outlineStyle());
        label.setAlignment(Align.center);
        label.setColor(RIBBON_TEXT);
        label.setFontScale(0.58f);
        host.add(label).padLeft(8f).padRight(8f);
        return host;
    }

    private Actor previewActor(ShopOffer offer) {
        if (hasText(offer.previewPlant())) {
            PamActor pam = new PamActor(assets.pamPlayer());
            pam.setClip(catalog.idleFor(offer.previewPlant()), 0.42f);
            pam.setTouchable(Touchable.disabled);
            return pam;
        }
        Image art = new Image(new TextureRegionDrawable(assets.region(previewImageId(offer))));
        art.setScaling(Scaling.fit);
        art.setTouchable(Touchable.disabled);
        return art;
    }

    private String previewImageId(ShopOffer offer) {
        if (!hasText(offer.previewImageId())) {
            return MenuAssetIds.SEED_PACKET_MULTI;
        }
        return knownImage(assets, offer.previewImageId())
                ? offer.previewImageId()
                : MenuAssetIds.SEED_PACKET_MULTI;
    }

    private static String chromeFor(ShopOffer offer) {
        if (offer.daily()) {
            return MenuAssetIds.STORE_CARD_DAILY;
        }
        return switch (offer.tab()) {
            case SEEDS -> MenuAssetIds.STORE_CARD_SEEDS;
            case COINS -> MenuAssetIds.STORE_CARD_COINS;
            case GARDEN -> MenuAssetIds.STORE_CARD_GARDEN;
        };
    }

    private String titleStyle() {
        if (skin.has("big_outline", Label.LabelStyle.class)) {
            return "big_outline";
        }
        return outlineStyle();
    }

    private float titleScale() {
        return skin.has("big_outline", Label.LabelStyle.class) ? 0.46f : 0.92f;
    }

    private String outlineStyle() {
        return skin.has("medium_outline", Label.LabelStyle.class) ? "medium_outline" : "medium";
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    public static String packetImageId(GameAssets assets, String plantName) {
        if (plantName == null || plantName.isBlank()) {
            return MenuAssetIds.SEED_PACKET_MULTI;
        }
        String mapped = PlantPacketIds.imageId(plantName);
        if (knownImage(assets, mapped)) {
            return mapped;
        }
        String id = MenuAssetIds.SEED_PACKET_PREFIX + PlantPacketIds.normalize(plantName);
        return knownImage(assets, id) ? id : MenuAssetIds.SEED_PACKET_MULTI;
    }

    public static boolean hasPacketImage(GameAssets assets, String plantName) {
        String imageId = packetImageId(assets, plantName);
        return imageId != null
                && !MenuAssetIds.SEED_PACKET_MULTI.equals(imageId)
                && !LawnAssetIds.PACKET_EMPTY.equals(imageId)
                && knownImage(assets, imageId);
    }

    public static Actor plantArt(GameAssets assets, String plantName, float pamScale) {
        if (hasPacketImage(assets, plantName)) {
            Image image = new Image(new TextureRegionDrawable(assets.region(packetImageId(assets, plantName))));
            image.setScaling(Scaling.fit);
            image.setTouchable(Touchable.disabled);
            return image;
        }
        PamActor pam = new PamActor(assets.pamPlayer());
        pam.freezeClip(assets.plantAnims().idleFor(plantName), pamScale);
        pam.setTouchable(Touchable.disabled);
        return pam;
    }

    private static boolean knownImage(GameAssets assets, String imageId) {
        return assets.resourceIndex().image(imageId) != null;
    }
}
