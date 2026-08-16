package io.github.finalwave.view.gui.widget;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import io.github.finalwave.model.collection.CollectionPlantEntry;
import io.github.finalwave.view.gui.assets.CollectionCardLooks;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.assets.MenuAssetIds;


public final class CollectionPlantCard extends Table {
    public static final float CARD_WIDTH = 150f;
    public static final float CARD_HEIGHT = 115f;

    private static Texture dimTexture;

    private static final float BADGE_SIZE = 38f;
    private static final float SHADOW_OFFSET = 4f;
    private static final float SHADOW_ALPHA = 0.38f;

    private final GameAssets assets;
    private final Skin skin;
    private MintFamilyBadge familyBadge;
    private Container<Label> levelContainer;
    private Image plantImage;
    private Image darkOverlay;
    private Image lockIcon;
    private boolean conveyorLook;
    private boolean selected;

    public CollectionPlantCard(GameAssets assets, Skin skin) {
        this.assets = assets;
        this.skin = skin;
        setTouchable(Touchable.enabled);
    }

    public void bind(CollectionPlantEntry entry) {
        bindInternal(entry, false);
    }

    public void bindConveyor(CollectionPlantEntry entry) {
        bindInternal(entry, true);
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        setTransform(true);
        setOrigin(Align.center);
        setScale(selected ? 1.06f : 1f);
    }

    public boolean selected() {
        return selected;
    }

    private void bindInternal(CollectionPlantEntry entry, boolean conveyor) {
        clearChildren();
        familyBadge = null;
        levelContainer = null;
        plantImage = null;
        darkOverlay = null;
        lockIcon = null;
        conveyorLook = conveyor;
        selected = false;
        setScale(1f);

        setBackground(new TextureRegionDrawable(assets.region(CollectionCardLooks.packetBackground(entry))));

        plantImage = new Image(new TextureRegionDrawable(
                assets.region(ShopItemCard.packetImageId(assets, entry.name()))));
        plantImage.setScaling(Scaling.fit);
        plantImage.setTouchable(Touchable.disabled);
        if (conveyor) {
            addActor(plantImage);
        } else {
            add(plantImage).size(98f).expand().center().padTop(8f).row();
            add(seedProgress(entry)).growX().height(16f).padBottom(6f).padLeft(10f).padRight(10f).bottom();
        }

        if (conveyor || entry.owned()) {
            Label level = new Label("LVL " + entry.level(), skin, outlineStyle());
            level.setFontScale(0.72f);
            level.setAlignment(Align.center);
            levelContainer = new Container<>(level);
            levelContainer.setTransform(true);
            levelContainer.setOrigin(Align.center);
            levelContainer.setRotation(-40f);
            levelContainer.setTouchable(Touchable.disabled);
            addActor(levelContainer);
        }

        if (!conveyor && !entry.owned()) {
            darkOverlay = new Image(dimDrawable());
            darkOverlay.setColor(0f, 0f, 0f, 0.65f);
            darkOverlay.setTouchable(Touchable.disabled);
            addActor(darkOverlay);
            lockIcon = new Image(new TextureRegionDrawable(assets.region(MenuAssetIds.ZEN_LOCKED_POT)));
            lockIcon.setScaling(Scaling.fit);
            lockIcon.setTouchable(Touchable.disabled);
            addActor(lockIcon);
        }

        MintFamilyBadge badge = new MintFamilyBadge(assets);
        badge.bind(entry.category());
        familyBadge = badge;
        addActor(badge);
    }

    private Stack seedProgress(CollectionPlantEntry entry) {
        ProgressBar bar = new ProgressBar(0f, 1f, 0.01f, false, skin, progressStyle());
        bar.setAnimateDuration(0f);
        int needed = Math.max(1, entry.seedPacketsNeeded());
        float value = entry.maxLevel() ? 1f : Math.min(1f, entry.seedPackets() / (float) needed);
        bar.setValue(value);
        Label fraction = new Label(seedLabel(entry), skin, outlineStyle());
        fraction.setAlignment(Align.center);
        fraction.setFontScale(0.55f);
        fraction.setColor(Color.WHITE);
        Stack stack = new Stack();
        stack.add(bar);
        stack.add(fraction);
        stack.setTouchable(Touchable.disabled);
        return stack;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        drawShadow(batch, parentAlpha);
        super.draw(batch, parentAlpha);
    }

    private void drawShadow(Batch batch, float parentAlpha) {
        Drawable background = getBackground();
        if (background == null) {
            return;
        }
        Color previous = batch.getColor();
        batch.setColor(0f, 0f, 0f, SHADOW_ALPHA * parentAlpha * getColor().a);
        background.draw(batch, getX() + SHADOW_OFFSET, getY() - SHADOW_OFFSET, getWidth(), getHeight());
        batch.setColor(previous);
    }

    @Override
    public void layout() {
        super.layout();
        if (conveyorLook && plantImage != null) {
            float plantHeight = getHeight() * 0.88f;
            float plantWidth = Math.min(getWidth() * 0.72f, plantHeight);
            plantImage.setSize(plantWidth, plantHeight);
            plantImage.setPosition(0f, (getHeight() - plantHeight) * 0.5f);
        }
        float badgeSize = conveyorLook ? Math.min(BADGE_SIZE, getHeight() * 0.38f) : BADGE_SIZE;
        if (familyBadge != null) {
            familyBadge.setSize(badgeSize, badgeSize);
            familyBadge.setPosition(-badgeSize * 0.28f, getHeight() - badgeSize * 0.72f);
        }
        if (levelContainer != null) {
            levelContainer.pack();
            float levelLift = conveyorLook ? getHeight() * 0.16f : 18f;
            levelContainer.setPosition(
                    getWidth() - levelContainer.getWidth(),
                    getHeight() - levelContainer.getHeight() + levelLift);
        }
        if (darkOverlay != null) {
            darkOverlay.setSize(getWidth(), getHeight());
            darkOverlay.setPosition(0f, 0f);
        }
        if (lockIcon != null) {
            lockIcon.setSize(45f, 55f);
            lockIcon.setPosition((getWidth() - lockIcon.getWidth()) / 2f, (getHeight() - lockIcon.getHeight()) / 2f);
        }
    }

    private String progressStyle() {
        return skin.has("xp_yellow", ProgressBar.ProgressBarStyle.class) ? "xp_yellow" : "xp_green";
    }

    private String outlineStyle() {
        return skin.has("medium_outline", Label.LabelStyle.class) ? "medium_outline" : "medium";
    }

    private static String seedLabel(CollectionPlantEntry entry) {
        if (entry.maxLevel()) {
            return "MAX";
        }
        return entry.seedPackets() + "/" + Math.max(1, entry.seedPacketsNeeded());
    }

    private static TextureRegionDrawable dimDrawable() {
        if (dimTexture == null) {
            Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixmap.setColor(Color.WHITE);
            pixmap.fill();
            dimTexture = new Texture(pixmap);
            pixmap.dispose();
        }
        return new TextureRegionDrawable(new TextureRegion(dimTexture));
    }
}
