package io.github.finalwave.view.gui.widget;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
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

    private final GameAssets assets;
    private final Skin skin;
    private final Image background;
    private final Image packet;
    private final Image cooldownShade;
    private final Image lockIcon;
    private final Image familyIcon;
    private final Image boostIcon;
    private final Label costLabel;
    private final Label levelLabel;
    private final Label seedLabel;
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
        this.skin = skin;
        setSize(WIDTH, HEIGHT);
        setTransform(true);
        setOrigin(Align.center);

        background = new Image(new TextureRegionDrawable(assets.region(LawnAssetIds.PACKET_BG)));
        background.setFillParent(true);
        background.setScaling(Scaling.stretch);

        packet = new Image();
        packet.setFillParent(true);
        packet.setScaling(Scaling.fit);

        cooldownShade = new Image(new TextureRegionDrawable(assets.region(LawnAssetIds.PACKET_BG)));
        cooldownShade.setColor(0f, 0f, 0f, 0.55f);
        cooldownShade.setTouchable(Touchable.disabled);
        cooldownShade.setVisible(false);

        lockIcon = new Image(new TextureRegionDrawable(assets.region(LawnAssetIds.PACKET_LOCK)));
        lockIcon.setSize(36f, 36f);
        lockIcon.setVisible(false);
        lockIcon.setTouchable(Touchable.disabled);

        familyIcon = new Image();
        familyIcon.setSize(28f, 28f);
        familyIcon.setVisible(false);
        familyIcon.setTouchable(Touchable.disabled);

        boostIcon = new Image(new TextureRegionDrawable(assets.region(MenuAssetIds.SPROUT_ICON)));
        boostIcon.setSize(26f, 26f);
        boostIcon.setVisible(false);
        boostIcon.setTouchable(Touchable.disabled);

        String outline = outlineStyle(skin);
        costLabel = new Label("", skin, outline);
        costLabel.setAlignment(Align.right);
        levelLabel = new Label("", skin, outline);
        levelLabel.setAlignment(Align.right);
        seedLabel = new Label("", skin, outline);
        seedLabel.setAlignment(Align.center);
        seedLabel.setVisible(false);

        addActor(background);
        addActor(packet);
        addActor(cooldownShade);
        addActor(familyIcon);
        addActor(lockIcon);
        addActor(boostIcon);
        addActor(levelLabel);
        addActor(costLabel);
        addActor(seedLabel);

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
        packet.setVisible(true);
        familyIcon.setVisible(false);
        boostIcon.setVisible(false);
        lockIcon.setVisible(false);
        costLabel.setText("");
        levelLabel.setText("");
        seedLabel.setVisible(false);
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
            familyIcon.setVisible(false);
            return;
        }
        String iconId = CollectionCardLooks.familyIcon(category);
        if (!assets.hasImage(iconId)) {
            familyIcon.setVisible(false);
            return;
        }
        familyIcon.setDrawable(new TextureRegionDrawable(assets.region(iconId)));
        familyIcon.setVisible(true);
    }

    public void setBoosted(boolean boosted) {
        this.boosted = boosted;
        boostIcon.setVisible(boosted && !empty);
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
        if (need <= 0) {
            seedLabel.setVisible(false);
            seedLabel.setText("");
            return;
        }
        seedLabel.setVisible(true);
        seedLabel.setText(have + "/" + need);
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

    private void layoutChildren() {
        if (costLabel == null) {
            return;
        }
        float width = getWidth();
        float height = getHeight();
        costLabel.setSize(width * 0.42f, 22f);
        costLabel.setPosition(width - costLabel.getWidth() - 6f, 4f);
        levelLabel.setSize(width * 0.5f, 20f);
        levelLabel.setPosition(width - levelLabel.getWidth() - 4f, height - 22f);
        seedLabel.setSize(width - 12f, 16f);
        seedLabel.setPosition(6f, 22f);
        familyIcon.setSize(Math.min(28f, height * 0.28f), Math.min(28f, height * 0.28f));
        familyIcon.setPosition(2f, height - familyIcon.getHeight() - 2f);
        boostIcon.setPosition(6f, 22f);
        lockIcon.setPosition((width - lockIcon.getWidth()) / 2f, (height - lockIcon.getHeight()) / 2f);
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
    }

    private static String outlineStyle(Skin skin) {
        if (skin.has("medium_outline", Label.LabelStyle.class)) {
            return "medium_outline";
        }
        return "medium";
    }
}
