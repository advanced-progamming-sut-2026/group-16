package io.github.finalwave.view.gui.widget;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import io.github.finalwave.model.collection.CollectionPlantEntry;
import io.github.finalwave.view.gui.assets.CollectionCardLooks;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.assets.LawnAssetIds;
import io.github.finalwave.view.gui.assets.MenuAssetIds;
import io.github.finalwave.view.gui.assets.PlantPacketIds;


public final class PlantCardActor extends Group {
    public static final float WIDTH = 140f;
    public static final float HEIGHT = 105f;

    private static final float REFERENCE_HEIGHT = 105f;
    private static final float SHADOW_OFFSET = 4f;
    private static final float SHADOW_ALPHA = 0.38f;
    private static final float LEVEL_FONT_SCALE = 0.68f;
    private static final String BIG_OUTLINE = "big_outline";
    private static final String MEDIUM_OUTLINE = "medium_outline";
    private static final Color BADGE_TINT = new Color(1f, 1f, 1f, 1f);

    private final GameAssets assets;
    private final Image background;
    private final Image packet;
    private final Image cooldownShade;
    private final Image lockIcon;
    private final MintFamilyBadge familyBadge;
    private final Label costLabel;
    private final Label levelLabel;
    private final UpgradeSeedBar seedBar;
    private final float costFontScale;
    private String plantName;
    private String packetBackgroundId = LawnAssetIds.PACKET_BG;
    private Runnable onClick;
    private boolean locked;
    private boolean disabled;
    private boolean boosted;
    private boolean selected;
    private boolean empty;

    public PlantCardActor(GameAssets assets, Skin skin, String plantName) {
        this.assets = assets;
        setSize(WIDTH, HEIGHT);
        setTransform(true);
        setOrigin(Align.center);

        background = new Image(new TextureRegionDrawable(assets.region(LawnAssetIds.PACKET_BG)));
        background.setFillParent(true);
        background.setScaling(Scaling.stretch);

        packet = new Image();
        packet.setScaling(Scaling.fit);

        cooldownShade = new Image(new TextureRegionDrawable(assets.region(LawnAssetIds.PACKET_BG)));
        cooldownShade.setColor(0f, 0f, 0f, 0.55f);
        cooldownShade.setTouchable(Touchable.disabled);
        cooldownShade.setVisible(false);

        lockIcon = new Image(new TextureRegionDrawable(assets.region(LawnAssetIds.PACKET_LOCK)));
        lockIcon.setVisible(false);
        lockIcon.setTouchable(Touchable.disabled);

        familyBadge = new MintFamilyBadge(assets);
        familyBadge.setVisible(false);

        costFontScale = skin.has(BIG_OUTLINE, Label.LabelStyle.class) ? 0.82f : 1.6f;
        costLabel = new Label("", skin, costStyle(skin));
        costLabel.setAlignment(Align.right);
        levelLabel = new Label("", skin, levelStyle(skin));
        levelLabel.setAlignment(Align.right);
        seedBar = new UpgradeSeedBar(skin);
        seedBar.setVisible(false);

        addActor(background);
        addActor(packet);
        addActor(cooldownShade);
        addActor(seedBar);
        addActor(lockIcon);
        addActor(levelLabel);
        addActor(costLabel);
        addActor(familyBadge);

        PvzButtons.animate(this, 1.08f, 0.92f, () -> {
            if (onClick != null && !empty) {
                onClick.run();
            }
        });
        setPlant(plantName);
        layoutChildren();
    }

    public void bind(CollectionPlantEntry entry) {
        if (entry == null) {
            setEmpty();
            return;
        }
        packetBackgroundId = CollectionCardLooks.packetBackground(entry);
        setPlant(entry.name());
        setLocked(!entry.owned());
        setLevel(entry.owned() ? entry.level() : 0);
        setFamily(entry.category());
        if (entry.owned() && !entry.maxLevel()) {
            setSeedProgress(entry.seedPackets(), entry.seedPacketsNeeded());
        } else {
            setSeedProgress(0, 0);
        }
        refreshBackground();
    }

    public void setEmpty() {
        empty = true;
        plantName = null;
        packetBackgroundId = LawnAssetIds.PACKET_EMPTY;
        packet.setDrawable(new TextureRegionDrawable(assets.region(LawnAssetIds.PACKET_EMPTY)));
        packet.setVisible(false);
        familyBadge.setVisible(false);
        lockIcon.setVisible(false);
        costLabel.setText("");
        levelLabel.setText("");
        seedBar.setVisible(false);
        locked = false;
        boosted = false;
        selected = false;
        setScale(1f);
        refreshBackground();
        refreshTint();
    }

    public void setPlant(String plantName) {
        this.plantName = plantName;
        empty = plantName == null || plantName.isBlank();
        if (empty) {
            setEmpty();
            return;
        }
        if (LawnAssetIds.PACKET_EMPTY.equals(packetBackgroundId)) {
            packetBackgroundId = LawnAssetIds.PACKET_BG;
        }
        String imageId = PlantPacketIds.imageId(plantName);
        if (!assets.hasImage(imageId)) {
            imageId = ShopItemCard.packetImageId(assets, plantName);
        }
        packet.setDrawable(new TextureRegionDrawable(assets.region(imageId)));
        packet.setVisible(true);
        refreshBackground();
    }

    public String plantName() {
        return plantName;
    }

    public void setCost(int cost) {
        costLabel.setText(cost <= 0 ? "" : String.valueOf(cost));
    }

    public void setCooldownRatio(float ratio) {
        float clamped = Math.max(0f, Math.min(1f, ratio));
        cooldownShade.setVisible(clamped > 0f);
        cooldownShade.setHeight(getHeight() * clamped);
        cooldownShade.setY(0f);
        cooldownShade.setWidth(getWidth());
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
        lockIcon.setVisible(locked && !empty);
        refreshTint();
    }

    public void setFamily(String category) {
        if (category == null || category.isBlank() || empty) {
            familyBadge.setVisible(false);
            return;
        }
        familyBadge.bind(category);
    }

    public void setBoosted(boolean boosted) {
        this.boosted = boosted;
        refreshBackground();
        refreshTint();
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        setScale(selected ? 1.06f : 1f);
    }

    public void setLevel(int level) {
        levelLabel.setText(level > 0 ? "LVL " + level : "");
    }

    public void setSeedProgress(int have, int need) {
        if (need <= 0 || empty) {
            seedBar.setVisible(false);
            return;
        }
        seedBar.setVisible(true);
        seedBar.bind(have / (float) need, "");
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
        refreshTint();
    }

    public void setOnClick(Runnable onClick) {
        this.onClick = onClick;
    }

    @Override
    public void setSize(float width, float height) {
        super.setSize(width, height);
        layoutChildren();
    }

    @Override
    protected void sizeChanged() {
        super.sizeChanged();
        layoutChildren();
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        drawShadow(batch, parentAlpha);
        super.draw(batch, parentAlpha);
    }

    private void drawShadow(Batch batch, float parentAlpha) {
        Drawable drawable = background.getDrawable();
        if (drawable == null) {
            return;
        }
        Color previous = batch.getColor();
        batch.setColor(0f, 0f, 0f, SHADOW_ALPHA * parentAlpha * getColor().a);
        drawable.draw(batch,
                getX() + SHADOW_OFFSET,
                getY() - SHADOW_OFFSET,
                getWidth(),
                getHeight());
        batch.setColor(previous);
    }

    private void layoutChildren() {
        if (costLabel == null) {
            return;
        }
        float width = getWidth();
        float height = getHeight();
        float scale = Math.min(width / WIDTH, height / REFERENCE_HEIGHT);

        packet.setBounds(width * 0.04f, height * 0.19f, width * 0.58f, height * 0.66f);

        costLabel.setFontScale(costFontScale * scale);
        costLabel.setSize(width * 0.46f, height * 0.44f);
        costLabel.setPosition(width * 0.48f, height * 0.2f);

        levelLabel.setFontScale(LEVEL_FONT_SCALE * scale);
        levelLabel.setSize(width * 0.6f, height * 0.18f);
        levelLabel.setPosition(width * 0.34f, height * 0.78f);

        float barHeight = Math.max(5f, height * 0.09f);
        seedBar.setBounds(width * 0.1f, height * 0.07f, width * 0.8f, barHeight);

        float badgeSize = height * 0.33f;
        familyBadge.setSize(badgeSize, badgeSize);
        familyBadge.setPosition(-badgeSize * 0.3f, height - badgeSize * 0.72f);

        float lockSize = Math.min(width, height) * 0.42f;
        lockIcon.setSize(lockSize, lockSize);
        lockIcon.setPosition((width - lockSize) / 2f, (height - lockSize) / 2f);

        cooldownShade.setWidth(width);
    }

    private void refreshBackground() {
        String id = boosted && !empty ? MenuAssetIds.PACKET_BOOST : packetBackgroundId;
        if (!assets.hasImage(id)) {
            id = LawnAssetIds.PACKET_BG;
        }
        background.setDrawable(new TextureRegionDrawable(assets.region(id)));
    }

    private void refreshTint() {
        Color tint = Color.WHITE;
        if (locked || disabled) {
            tint = Color.GRAY;
        } else if (boosted) {
            tint = new Color(1f, 0.92f, 0.55f, 1f);
        }
        background.setColor(tint);
        packet.setColor(tint);
        familyBadge.setColor(locked || disabled ? Color.LIGHT_GRAY : BADGE_TINT);
    }

    private static String costStyle(Skin skin) {
        if (skin.has(BIG_OUTLINE, Label.LabelStyle.class)) {
            return BIG_OUTLINE;
        }
        return levelStyle(skin);
    }

    private static String levelStyle(Skin skin) {
        return skin.has(MEDIUM_OUTLINE, Label.LabelStyle.class) ? MEDIUM_OUTLINE : "medium";
    }
}
