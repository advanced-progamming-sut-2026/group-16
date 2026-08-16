package io.github.finalwave.view.gui.hud;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.assets.LawnAssetIds;
import io.github.finalwave.view.gui.widget.PvzButtons;

import java.util.function.BiConsumer;


public final class PlantFoodCounter extends WidgetGroup {
    public static final int SLOT_COUNT = GameSession.MAX_PLANT_FOOD;
    private static final float BANK_NATIVE_W = 206f;
    private static final float BANK_NATIVE_H = 88f;
    private static final float BANK_W = 248f;
    private static final float BANK_H = 106f;
    private static final float PLUS_SIZE = 56f;
    private static final float PLUS_GAP = 14f;
    private static final float ORB_CX = 44f / BANK_NATIVE_W;
    private static final float ORB_CY = 43.8f / BANK_NATIVE_H;
    private static final float[] SLOT_CX = {
            88f / BANK_NATIVE_W,
            112f / BANK_NATIVE_W,
            136f / BANK_NATIVE_W,
            160f / BANK_NATIVE_W,
            184f / BANK_NATIVE_W
    };
    private static final float SLOT_CY = 44f / BANK_NATIVE_H;

    private final Image bank;
    private final Image orbLeaf;
    private final Image[] slots = new Image[SLOT_COUNT];
    private final Actor plus;
    private final Runnable onAdd;
    private final Runnable onDragStart;
    private final BiConsumer<Float, Float> onDrop;
    private int count;
    private boolean dragging;

    public PlantFoodCounter(GameAssets assets,
                            Runnable onAdd,
                            Runnable onDragStart,
                            BiConsumer<Float, Float> onDrop) {
        this.onAdd = onAdd;
        this.onDragStart = onDragStart;
        this.onDrop = onDrop;
        setTransform(false);
        bank = new Image(new TextureRegionDrawable(assets.region(LawnAssetIds.PLANTFOOD_BANK)));
        bank.setScaling(Scaling.stretch);
        bank.setTouchable(Touchable.enabled);
        addActor(bank);

        orbLeaf = image(assets, LawnAssetIds.PLANTFOOD_LEAF);
        addActor(orbLeaf);

        for (int i = 0; i < SLOT_COUNT; i++) {
            slots[i] = image(assets, LawnAssetIds.PLANTFOOD_SLOT);
            addActor(slots[i]);
        }

        plus = PvzButtons.iconButton(assets.region(LawnAssetIds.HUD_PLUS), PLUS_SIZE, PLUS_SIZE, this::addFood);
        addActor(plus);
        bank.addListener(new DragListener());
        setSize(getPrefWidth(), getPrefHeight());
        refreshSlots();
    }

    public void setCount(int count) {
        this.count = Math.max(0, Math.min(SLOT_COUNT, count));
        refreshSlots();
    }

    @Override
    public float getPrefWidth() {
        return BANK_W + PLUS_GAP + PLUS_SIZE;
    }

    @Override
    public float getPrefHeight() {
        return Math.max(BANK_H, PLUS_SIZE);
    }

    @Override
    public void layout() {
        bank.setBounds(0f, (getHeight() - BANK_H) * 0.5f, BANK_W, BANK_H);
        float leaf = BANK_H * 0.48f;
        centerOnBank(orbLeaf, ORB_CX, ORB_CY, leaf, leaf, leaf * 0.05f, -leaf * 0.08f);
        float slot = BANK_H * 0.28f;
        for (int i = 0; i < SLOT_COUNT; i++) {
            centerOnBank(slots[i], SLOT_CX[i], SLOT_CY, slot, slot, 0f, 0f);
        }
        plus.setSize(PLUS_SIZE, PLUS_SIZE);
        plus.setPosition(BANK_W + PLUS_GAP, (getHeight() - PLUS_SIZE) * 0.5f);
    }

    private void addFood() {
        if (onAdd != null && count < SLOT_COUNT) {
            onAdd.run();
        }
    }

    private void refreshSlots() {
        int shown = dragging ? Math.max(0, count - 1) : count;
        orbLeaf.setVisible(true);
        orbLeaf.getColor().a = count > 0 ? 1f : 0.45f;
        for (int i = 0; i < SLOT_COUNT; i++) {
            slots[i].setVisible(i < shown);
        }
    }

    private void centerOnBank(Image image, float nx, float ny, float width, float height, float ox, float oy) {
        image.setSize(width, height);
        image.setPosition(
                bank.getX() + nx * bank.getWidth() - width * 0.5f + ox,
                bank.getY() + ny * bank.getHeight() - height * 0.5f + oy);
    }

    private static Image image(GameAssets assets, String id) {
        Image image = new Image(new TextureRegionDrawable(assets.region(id)));
        image.setScaling(Scaling.fit);
        image.setTouchable(Touchable.disabled);
        return image;
    }

    private final class DragListener extends InputListener {
        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            if (count <= 0) {
                return false;
            }
            dragging = true;
            refreshSlots();
            if (onDragStart != null) {
                onDragStart.run();
            }
            return true;
        }

        @Override
        public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
            dragging = false;
            refreshSlots();
            if (onDrop != null) {
                onDrop.accept(event.getStageX(), event.getStageY());
            }
        }
    }
}
