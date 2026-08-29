package io.github.finalwave.view.gui.widget;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
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
import io.github.finalwave.model.greenhouse.GreenhouseLayout;
import io.github.finalwave.model.greenhouse.GreenhouseSlotState;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.assets.LawnAssetIds;
import io.github.finalwave.view.gui.assets.MenuAssetIds;
import io.github.finalwave.view.gui.assets.PlantAnimationCatalog;


public final class GreenhousePotSlot extends Group {
    public interface Actions {
        void plant();

        void collect();

        void grow();

        void unlock();
    }

    private static final float GLOW_WIDTH = 130f;
    private static final float GLOW_HEIGHT = 210f;
    private static final float LOCK_WIDTH = 34f;
    private static final float LOCK_HEIGHT = 46f;
    private static final float UNLOCK_TAG_WIDTH = 140f;
    private static final float UNLOCK_TAG_HEIGHT = 58f;
    private static final float TIMER_WIDTH = 100f;
    private static final float TIMER_HEIGHT = 36f;
    private static final float SPEEDUP_WIDTH = 64f;
    private static final float GEM_ICON_SIZE = 26f;
    private static final float GEM_HANG = 18f;
    private static final float TIMER_UNDER_BUTTON = 22f;

    private final GameAssets assets;
    private final PlantAnimationCatalog catalog;
    private final Skin skin;
    private final PamActor potActor;
    private final Image potImage;
    private final PamActor plantActor;
    private final PamActor poofActor;
    private final Image lockIcon;
    private final Image glow;
    private final Image timerBackground;
    private final Label timerLabel;
    private final Label speedUpCost;
    private final TextButton plantButton;
    private final TextButton speedUpButton;
    private final Stack unlockTag;
    private final Table overlay;
    private GreenhouseSlotState state;
    private Actions actions;
    private boolean grownInPot;
    private int displayedCost = -1;

    public GreenhousePotSlot(GameAssets assets, PlantAnimationCatalog catalog, Skin skin) {
        this.assets = assets;
        this.catalog = catalog;
        this.skin = skin;
        setSize(GreenhouseGrid.SLOT_WIDTH, GreenhouseGrid.SLOT_HEIGHT);
        setTransform(false);
        setTouchable(Touchable.childrenOnly);

        glow = image(MenuAssetIds.ZEN_BOOST_GLOW, GLOW_WIDTH, GLOW_HEIGHT);
        glow.setPosition(
                GreenhouseGrid.pamOriginX() - GLOW_WIDTH * 0.5f,
                GreenhouseGrid.pamOriginY() - 40f);
        glow.setVisible(false);
        glow.setTouchable(Touchable.disabled);
        addActor(glow);

        potActor = pamLayer();
        addActor(potActor);

        potImage = image(MenuAssetIds.ZEN_POT, GreenhouseGrid.POT_WIDTH, GreenhouseGrid.POT_HEIGHT);
        potImage.setPosition(GreenhouseGrid.potImageX(), GreenhouseGrid.potImageY());
        potImage.setTouchable(Touchable.disabled);
        potImage.setVisible(false);
        addActor(potImage);

        plantActor = pamLayer();
        plantActor.setAnchor(0.5f, GreenhouseGrid.PLANT_FEET_ANCHOR_Y);
        plantActor.setY(GreenhouseGrid.soilY() - GreenhouseGrid.PAM_HEIGHT * GreenhouseGrid.PLANT_FEET_ANCHOR_Y);
        addActor(plantActor);

        poofActor = pamLayer();
        poofActor.setAnchor(0.5f, GreenhouseGrid.PLANT_FEET_ANCHOR_Y);
        poofActor.setY(GreenhouseGrid.soilY() - GreenhouseGrid.PAM_HEIGHT * GreenhouseGrid.PLANT_FEET_ANCHOR_Y);
        poofActor.setVisible(false);
        addActor(poofActor);

        lockIcon = image(LawnAssetIds.PACKET_LOCK, LOCK_WIDTH, LOCK_HEIGHT);
        lockIcon.setPosition((GreenhouseGrid.SLOT_WIDTH - LOCK_WIDTH) * 0.5f, GreenhouseGrid.PLANT_ANCHOR_Y + 24f);
        lockIcon.setVisible(false);
        lockIcon.setTouchable(Touchable.disabled);
        addActor(lockIcon);

        Actor potHit = new Actor();
        potHit.setSize(GreenhouseGrid.HIT_WIDTH, GreenhouseGrid.HIT_HEIGHT);
        potHit.setPosition(
                GreenhouseGrid.PLANT_ANCHOR_X - GreenhouseGrid.HIT_WIDTH * 0.5f,
                GreenhouseGrid.PLANT_ANCHOR_Y - GreenhouseGrid.HIT_HEIGHT * 0.5f);
        PvzButtons.animate(potHit, 1.05f, 0.95f, this::onClicked);
        potHit.addListener(new com.badlogic.gdx.scenes.scene2d.InputListener() {
            @Override
            public void enter(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y, int pointer, com.badlogic.gdx.scenes.scene2d.Actor fromActor) {
                if (state != null && isMint(state.plantType()) && state.isReady(System.currentTimeMillis())) {
                    PlantAnimationCatalog.ClipSpec idle = catalog.idleFor(state.plantType());
                    plantActor.setClip(new PlantAnimationCatalog.ClipSpec(idle.path(), "loop"), GreenhouseGrid.READY_SCALE);
                }
            }

            @Override
            public void exit(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y, int pointer, com.badlogic.gdx.scenes.scene2d.Actor toActor) {
                if (state != null && isMint(state.plantType()) && state.isReady(System.currentTimeMillis())) {
                    plantActor.setClip(catalog.idleFor(state.plantType()), GreenhouseGrid.READY_SCALE);
                }
            }
        });
        addActor(potHit);

        overlay = new Table();
        overlay.setSize(GreenhouseGrid.SLOT_WIDTH, 52f);
        overlay.setPosition(0f, GreenhouseGrid.OVERLAY_Y);
        overlay.setTouchable(Touchable.childrenOnly);
        overlay.defaults().space(0f).pad(0f);
        overlay.center();
        addActor(overlay);

        timerBackground = new Image(new TextureRegionDrawable(assets.region(MenuAssetIds.ZEN_TIMER_BACKGROUND)));
        timerBackground.setScaling(Scaling.stretch);
        timerBackground.setFillParent(true);

        String style = skin.has("medium_outline", Label.LabelStyle.class) ? "medium_outline" : "medium";
        timerLabel = new Label("0h 0m", skin, style);
        timerLabel.setAlignment(Align.center);
        timerLabel.setColor(Color.WHITE);
        timerLabel.setFontScale(0.65f);

        speedUpCost = new Label("0", skin, style);
        speedUpCost.setAlignment(Align.right);
        speedUpCost.setColor(Color.WHITE);
        speedUpCost.setFontScale(0.72f);
        Image gemIcon = image(MenuAssetIds.ZEN_GEM_LARGE, GEM_ICON_SIZE, GEM_ICON_SIZE);
        speedUpButton = PvzButtons.textButton("", skin, "purple", () -> {
            if (actions != null) {
                actions.grow();
            }
        });
        speedUpButton.clearChildren();
        speedUpButton.left();
        speedUpButton.add(gemIcon).size(GEM_ICON_SIZE, GEM_ICON_SIZE).padLeft(-GEM_HANG).padRight(2f);
        speedUpButton.add(speedUpCost).expandX().right().padRight(6f);

        plantButton = PvzButtons.textButton("Plant", skin, "green_small", () -> {
            if (actions != null) {
                actions.plant();
            }
        });

        unlockTag = hangingUnlockTag(style);
        addActor(unlockTag);
    }

    public void bind(GreenhouseSlotState slot, Actions actions) {
        this.state = slot;
        this.actions = actions;
        this.grownInPot = slot != null && slot.isReady(System.currentTimeMillis());
        this.displayedCost = -1;
        poofActor.setClip(null, 1f);
        poofActor.setVisible(false);
        rebuildOverlay();
        refreshVisuals(System.currentTimeMillis());
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (state == null || state.locked() || state.empty()) {
            return;
        }
        long now = System.currentTimeMillis();
        if (!state.isReady(now)) {
            timerLabel.setText(state.remainingLabel(now));
            int cost = state.accelerateCost(now);
            if (cost != displayedCost) {
                displayedCost = cost;
                speedUpCost.setText(String.valueOf(cost));
            }
            return;
        }
        if (!grownInPot) {
            grownInPot = true;
            startGrowInPot();
        }
        if (poofActor.isVisible() && poofActor.stateTime() >= 1f) {
            poofActor.setClip(null, 1f);
            poofActor.setVisible(false);
        }
    }

    private void onClicked() {
        if (actions == null || state == null) {
            return;
        }
        long now = System.currentTimeMillis();
        if (state.locked()) {
            actions.unlock();
        } else if (state.empty()) {
            actions.plant();
        } else if (state.isReady(now)) {
            actions.collect();
        }
    }

    private void rebuildOverlay() {
        overlay.clearChildren();
        unlockTag.setVisible(false);
        if (state == null) {
            return;
        }
        long now = System.currentTimeMillis();
        if (state.locked()) {
            unlockTag.setVisible(true);
            return;
        }
        if (state.empty()) {
            overlay.add(plantButton).width(140f).height(44f);
            return;
        }
        if (state.isReady(now)) {
            return;
        }
        displayedCost = state.accelerateCost(now);
        speedUpCost.setText(String.valueOf(displayedCost));
        Stack timerChip = new Stack();
        timerChip.add(timerBackground);
        Table labelTable = new Table();
        labelTable.add(timerLabel).grow().padRight(TIMER_UNDER_BUTTON);
        timerChip.add(labelTable);
        overlay.add(timerChip).size(TIMER_WIDTH, TIMER_HEIGHT);
        overlay.add(speedUpButton).size(SPEEDUP_WIDTH, TIMER_HEIGHT).padLeft(-TIMER_UNDER_BUTTON);
        timerLabel.setText(state.remainingLabel(now));
    }

    private void refreshVisuals(long now) {
        boolean locked = state != null && state.locked();
        boolean empty = state != null && state.empty();
        boolean ready = state != null && state.isReady(now);
        lockIcon.setVisible(locked);
        unlockTag.setVisible(locked);
        glow.setVisible(ready);
        applyDrawOffset();
        if (state == null || locked) {
            potActor.setClip(null, 1f);
            potImage.setVisible(false);
            plantActor.setClip(null, 1f);
            return;
        }
        if (empty) {
            potImage.setVisible(false);
            potActor.setClip(PlantAnimationCatalog.GROWING_SLOT, GreenhouseGrid.EMPTY_SCALE);
            plantActor.setClip(null, 1f);
            return;
        }
        potActor.setClip(null, 1f);
        potImage.setVisible(true);
        applyPotDrawable(ready);
        if (ready) {
            plantActor.setClip(catalog.idleFor(state.plantType()), GreenhouseGrid.READY_SCALE);
            return;
        }
        plantActor.playThen(
                PlantAnimationCatalog.SPROUT_PLANT.path(),
                PlantAnimationCatalog.SPROUT_PLANT.clip(),
                GreenhouseGrid.GROWING_SCALE,
                PlantAnimationCatalog.SPROUT.clip(),
                true,
                null);
        timerLabel.setText(state.remainingLabel(now));
    }

    private void applyDrawOffset() {
        float extraX = state == null ? 0f : GreenhouseGrid.plantOffsetX(state.x());
        float extraY = state == null ? 0f : GreenhouseGrid.plantOffsetY(state.y());
        potActor.setDrawOffset(extraX + GreenhouseGrid.EMPTY_OFFSET_X, extraY + GreenhouseGrid.EMPTY_OFFSET_Y);
        potImage.setPosition(GreenhouseGrid.potImageX() + extraX, GreenhouseGrid.potImageY() + extraY);
        float plantX = GreenhouseGrid.soilX() - GreenhouseGrid.PLANT_ANCHOR_X + extraX;
        float plantY = extraY;
        plantActor.setDrawOffset(plantX, plantY);
        poofActor.setDrawOffset(plantX, plantY);
    }

    private void applyPotDrawable(boolean ready) {
        String id = ready ? MenuAssetIds.ZEN_POT_PLANTED : MenuAssetIds.ZEN_POT;
        potImage.setDrawable(new TextureRegionDrawable(assets.region(id)));
    }

    private void startGrowInPot() {
        rebuildOverlay();
        applyPotDrawable(true);
        plantActor.setClip(catalog.idleFor(state.plantType()), GreenhouseGrid.READY_SCALE);
        poofActor.setVisible(true);
        poofActor.setClip(PlantAnimationCatalog.PLANT_POOF, GreenhouseGrid.READY_SCALE, false);
        glow.setVisible(true);
    }

    private PamActor pamLayer() {
        PamActor actor = new PamActor(assets.pamPlayer());
        actor.setSize(GreenhouseGrid.SLOT_WIDTH, GreenhouseGrid.PAM_HEIGHT);
        actor.setPosition(0f, GreenhouseGrid.PAM_Y);
        actor.setTouchable(Touchable.disabled);
        return actor;
    }

    private Stack hangingUnlockTag(String style) {
        Image plate = image(MenuAssetIds.ZEN_UNLOCK_BUTTON, UNLOCK_TAG_WIDTH, UNLOCK_TAG_HEIGHT);
        plate.setFillParent(true);
        Label price = new Label(
                PriceButton.amount(GreenhouseLayout.POT_UNLOCK_COST_DIAMONDS),
                skin,
                style);
        price.setAlignment(Align.center);
        price.setColor(Color.WHITE);
        price.setFontScale(0.78f);
        Table pricePad = new Table();
        pricePad.add(price).expand().center().padLeft(38f).padRight(10f);
        Stack tag = new Stack();
        tag.add(plate);
        tag.add(pricePad);
        tag.setSize(UNLOCK_TAG_WIDTH, UNLOCK_TAG_HEIGHT);
        tag.setPosition((GreenhouseGrid.SLOT_WIDTH - UNLOCK_TAG_WIDTH) * 0.5f + 10f, GreenhouseGrid.OVERLAY_Y - 4f);
        tag.setVisible(false);
        PvzButtons.animate(tag, 1.06f, 0.94f, () -> {
            if (actions != null) {
                actions.unlock();
            }
        });
        tag.setRotation(-12f);
        return tag;
    }

    private Image image(String id, float width, float height) {
        TextureRegion region = assets.region(id);
        Image image = new Image(new TextureRegionDrawable(region));
        image.setSize(width, height);
        image.setScaling(Scaling.fit);
        return image;
    }

    private static boolean isMint(String name) {
        if (name == null) {
            return false;
        }
        String lower = name.toLowerCase();
        return lower.equals("enlighten-mint")
                || lower.equals("appease-mint")
                || lower.equals("arma-mint")
                || lower.equals("bombard-mint")
                || lower.equals("enforce-mint")
                || lower.equals("reinforce-mint")
                || lower.equals("enchant-mint");
    }
}
