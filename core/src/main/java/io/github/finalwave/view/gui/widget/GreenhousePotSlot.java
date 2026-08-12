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
import io.github.finalwave.view.gui.assets.MenuAssetIds;
import io.github.finalwave.view.gui.assets.PlantAnimationCatalog;


public final class GreenhousePotSlot extends Group {
    public interface Actions {
        void plant();

        void collect();

        void grow();

        void unlock();
    }

    private final GameAssets assets;
    private final PlantAnimationCatalog catalog;
    private final PamActor pamActor;
    private final PamActor poofActor;
    private final Image lockIcon;
    private final Image glow;
    private final Image timerBackground;
    private final Label timerLabel;
    private final Actor gemButton;
    private final TextButton plantButton;
    private final TextButton unlockButton;
    private final Table overlay;
    private GreenhouseSlotState state;
    private Actions actions;
    private boolean grownInPot;

    public GreenhousePotSlot(GameAssets assets, PlantAnimationCatalog catalog, Skin skin) {
        this.assets = assets;
        this.catalog = catalog;
        setSize(GreenhouseGrid.SLOT_WIDTH, GreenhouseGrid.SLOT_HEIGHT);
        setTransform(false);
        setTouchable(Touchable.childrenOnly);

        pamActor = new PamActor(assets.pamPlayer());
        pamActor.setSize(GreenhouseGrid.SLOT_WIDTH, 220f);
        pamActor.setPosition(0f, 40f);
        pamActor.setTouchable(Touchable.disabled);
        addActor(pamActor);

        poofActor = new PamActor(assets.pamPlayer());
        poofActor.setSize(GreenhouseGrid.SLOT_WIDTH, 220f);
        poofActor.setPosition(0f, 40f);
        poofActor.setVisible(false);
        poofActor.setTouchable(Touchable.disabled);
        addActor(poofActor);

        glow = image(MenuAssetIds.ZEN_BOOST_GLOW, 180f, 180f);
        glow.setPosition(20f, 70f);
        glow.setVisible(false);
        glow.setTouchable(Touchable.disabled);
        addActor(glow);

        lockIcon = image(MenuAssetIds.ZEN_LOCKED_POT, 110f, 110f);
        lockIcon.setPosition(55f, 90f);
        lockIcon.setVisible(false);
        lockIcon.setTouchable(Touchable.disabled);
        addActor(lockIcon);

        Actor potHit = new Actor();
        potHit.setSize(GreenhouseGrid.HIT_WIDTH, GreenhouseGrid.HIT_HEIGHT);
        potHit.setPosition(
                GreenhouseGrid.PLANT_ANCHOR_X - GreenhouseGrid.HIT_WIDTH * 0.5f,
                GreenhouseGrid.PLANT_ANCHOR_Y - GreenhouseGrid.HIT_HEIGHT * 0.5f);
        PvzButtons.animate(potHit, 1.05f, 0.95f, this::onClicked);
        addActor(potHit);

        overlay = new Table();
        overlay.setSize(GreenhouseGrid.SLOT_WIDTH, 52f);
        overlay.setPosition(0f, GreenhouseGrid.OVERLAY_Y);
        overlay.setTouchable(Touchable.childrenOnly);
        addActor(overlay);

        timerBackground = new Image(new TextureRegionDrawable(assets.region(MenuAssetIds.ZEN_TIMER_BACKGROUND)));
        timerBackground.setScaling(Scaling.stretch);

        String style = skin.has("medium_outline", Label.LabelStyle.class) ? "medium_outline" : "medium";
        timerLabel = new Label("0h 0m", skin, style);
        timerLabel.setAlignment(Align.center);
        timerLabel.setColor(Color.WHITE);
        timerLabel.setFontScale(0.65f);

        gemButton = PvzButtons.iconButton(assets.region(MenuAssetIds.ZEN_GEM_LARGE), 48f, 48f, () -> {
            if (actions != null) {
                actions.grow();
            }
        });

        plantButton = PvzButtons.textButton("Plant", skin, "green_small", () -> {
            if (actions != null) {
                actions.plant();
            }
        });
        unlockButton = PvzButtons.textButton(
                GreenhouseLayout.POT_UNLOCK_COST_COINS + " coins",
                skin,
                "purple",
                () -> {
                    if (actions != null) {
                        actions.unlock();
                    }
                });

        overlay.center();
    }

    public void bind(GreenhouseSlotState slot, Actions actions) {
        this.state = slot;
        this.actions = actions;
        this.grownInPot = slot != null && slot.isReady(System.currentTimeMillis());
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
        if (state == null) {
            return;
        }
        long now = System.currentTimeMillis();
        if (state.locked()) {
            overlay.add(unlockButton).width(170f).height(44f);
            return;
        }
        if (state.empty()) {
            overlay.add(plantButton).width(140f).height(44f);
            return;
        }
        if (state.isReady(now)) {
            return;
        }
        Stack timerChip = new Stack();
        timerChip.add(timerBackground);
        Table labelTable = new Table();
        labelTable.add(timerLabel).growX();
        timerChip.add(labelTable);
        overlay.add(timerChip).size(148f, 36f);
        overlay.add(gemButton).size(48f).padLeft(8f);
    }

    private void refreshVisuals(long now) {
        lockIcon.setVisible(state != null && state.locked());
        glow.setVisible(state != null && state.isReady(now));
        applyDrawOffset();
        if (state == null || state.locked()) {
            pamActor.setClip(null, 1f);
            return;
        }
        if (state.empty()) {
            pamActor.setClip(PlantAnimationCatalog.GROWING_SLOT, GreenhouseGrid.EMPTY_SCALE);
            return;
        }
        if (state.isReady(now)) {
            pamActor.setClip(catalog.idleFor(state.plantType()), GreenhouseGrid.READY_SCALE);
            return;
        }
        pamActor.setClip(PlantAnimationCatalog.SPROUT, GreenhouseGrid.GROWING_SCALE);
        timerLabel.setText(state.remainingLabel(now));
    }

    private void applyDrawOffset() {
        if (state == null) {
            pamActor.setDrawOffset(0f, 0f);
            poofActor.setDrawOffset(0f, 0f);
            return;
        }
        float extraX = 0f;
        float extraY = 0f;
        if (!state.locked() && state.empty()) {
            extraX = GreenhouseGrid.EMPTY_OFFSET_X;
            extraY = GreenhouseGrid.EMPTY_OFFSET_Y;
        }
        float offsetX = GreenhouseGrid.plantOffsetX(state.x()) + extraX;
        float offsetY = GreenhouseGrid.plantOffsetY(state.y()) + extraY;
        pamActor.setDrawOffset(offsetX, offsetY);
        poofActor.setDrawOffset(offsetX, offsetY);
    }

    private void startGrowInPot() {
        rebuildOverlay();
        pamActor.setClip(catalog.idleFor(state.plantType()), GreenhouseGrid.READY_SCALE);
        poofActor.setVisible(true);
        poofActor.setClip(PlantAnimationCatalog.PLANT_POOF, GreenhouseGrid.READY_SCALE, false);
        glow.setVisible(true);
    }

    private Image image(String id, float width, float height) {
        TextureRegion region = assets.region(id);
        Image image = new Image(new TextureRegionDrawable(region));
        image.setSize(width, height);
        image.setScaling(Scaling.fit);
        return image;
    }
}
