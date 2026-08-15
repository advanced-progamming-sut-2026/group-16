package io.github.finalwave.view.gui.widget;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
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
import io.github.finalwave.view.gui.assets.GameAssets;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public final class StoreTopTabs<T> extends Stack {
    public static final float TAB_WIDTH = 124f;
    public static final float TAB_IDLE_HEIGHT = 100f;
    public static final float TAB_CHEVRON_HEIGHT = 24f;
    public static final float TAB_ACTIVE_HEIGHT = TAB_IDLE_HEIGHT + TAB_CHEVRON_HEIGHT;
    public static final float TAB_OVERLAP = TAB_CHEVRON_HEIGHT;

    private static final float TAB_ICON_ACTIVE = 62f;
    private static final float TAB_ICON_IDLE = 52f;
    private static final int TAB_BODY_SRC = 35;
    private static final int TAB_CHEVRON_INSET = 26;
    private static final int TAB_CHEVRON_SRC = 18;
    private static final float DEFAULT_LABEL_SCALE = 0.82f;
    private static final Color IDLE_CONTENT = new Color(1f, 1f, 1f, 0.78f);

    private final GameAssets assets;
    private final Skin skin;
    private final List<Tab<T>> tabs;
    private final Consumer<T> onSelect;
    private final float labelScale;
    private T active;

    public StoreTopTabs(
            GameAssets assets,
            Skin skin,
            List<Tab<T>> tabs,
            T active,
            Consumer<T> onSelect) {
        this(assets, skin, tabs, active, onSelect, DEFAULT_LABEL_SCALE);
    }

    public StoreTopTabs(
            GameAssets assets,
            Skin skin,
            List<Tab<T>> tabs,
            T active,
            Consumer<T> onSelect,
            float labelScale) {
        this.assets = Objects.requireNonNull(assets);
        this.skin = Objects.requireNonNull(skin);
        this.tabs = List.copyOf(tabs);
        this.active = active;
        this.onSelect = onSelect;
        this.labelScale = labelScale;
        setTouchable(Touchable.childrenOnly);
        rebuild();
    }

    public void select(T value) {
        if (value == null || Objects.equals(active, value)) {
            return;
        }
        active = value;
        rebuild();
        if (onSelect != null) {
            onSelect.accept(value);
        }
    }

    private void rebuild() {
        clearChildren();
        Table idleLayer = new Table();
        idleLayer.setTouchable(Touchable.childrenOnly);
        idleLayer.top().left();
        Table activeLayer = new Table();
        activeLayer.setTouchable(Touchable.childrenOnly);
        activeLayer.top().left();
        idleLayer.defaults().size(TAB_WIDTH, TAB_IDLE_HEIGHT).padRight(8f).top();
        activeLayer.defaults().size(TAB_WIDTH, TAB_ACTIVE_HEIGHT).padRight(8f).top();

        for (Tab<T> tab : tabs) {
            boolean selected = Objects.equals(active, tab.value());
            if (selected) {
                idleLayer.add().size(TAB_WIDTH, TAB_IDLE_HEIGHT).padRight(8f);
                activeLayer.add(tabButton(tab, true));
            } else {
                idleLayer.add(tabButton(tab, false));
                activeLayer.add().size(TAB_WIDTH, TAB_ACTIVE_HEIGHT).padRight(8f);
            }
        }
        add(idleLayer);
        add(activeLayer);
    }

    private Actor tabButton(Tab<T> tab, boolean selected) {
        float height = selected ? TAB_ACTIVE_HEIGHT : TAB_IDLE_HEIGHT;
        Stack stack = new Stack();
        stack.setSize(TAB_WIDTH, height);
        stack.setTouchable(Touchable.enabled);
        stack.add(tabPlate(assets.region(selected ? tab.activePlateId() : tab.idlePlateId()), selected));

        Table content = new Table();
        content.setTouchable(Touchable.disabled);
        content.top();
        if (tab.iconId() != null && !tab.iconId().isBlank()) {
            Image icon = new Image(new TextureRegionDrawable(assets.region(tab.iconId())));
            icon.setScaling(Scaling.fit);
            if (!selected) {
                icon.setColor(IDLE_CONTENT);
            }
            content.add(icon)
                    .size(selected ? TAB_ICON_ACTIVE : TAB_ICON_IDLE)
                    .padTop(8f)
                    .padBottom(selected ? TAB_CHEVRON_HEIGHT : 14f);
        } else {
            Label label = new Label(tab.label(), skin, "medium");
            label.setAlignment(Align.center);
            label.setColor(selected ? Color.WHITE : IDLE_CONTENT);
            label.setFontScale(labelScale);
            content.add(label)
                    .grow()
                    .padTop(8f)
                    .padBottom(selected ? TAB_CHEVRON_HEIGHT : 12f);
        }
        stack.add(content);
        PvzButtons.animate(stack, selected ? 1.02f : 1.08f, 0.94f, () -> select(tab.value()));
        return stack;
    }

    private static Actor tabPlate(TextureRegion region, boolean active) {
        int srcW = region.getRegionWidth();
        int srcH = region.getRegionHeight();
        int bodySrcH = Math.min(srcH, TAB_BODY_SRC);
        TextureRegion bodyRegion = new TextureRegion(region, 0, 0, srcW, bodySrcH);
        Image body = new Image(StoreChrome.tabBody(bodyRegion));
        body.setTouchable(Touchable.disabled);

        if (!active || srcW <= TAB_CHEVRON_INSET * 2 || srcH <= bodySrcH) {
            Table plate = new Table();
            plate.setFillParent(true);
            plate.setTouchable(Touchable.disabled);
            plate.add(body).grow();
            return plate;
        }
        int chevronH = Math.min(TAB_CHEVRON_SRC, srcH - bodySrcH);
        int chevronW = srcW - TAB_CHEVRON_INSET * 2;
        Image chevron = new Image(new TextureRegionDrawable(
                new TextureRegion(region, TAB_CHEVRON_INSET, bodySrcH, chevronW, chevronH)));
        chevron.setScaling(Scaling.stretch);
        chevron.setTouchable(Touchable.disabled);
        Table plate = new Table();
        plate.setFillParent(true);
        plate.setTouchable(Touchable.disabled);
        plate.add(body).grow();
        plate.row();
        plate.add(chevron).growX().height(TAB_CHEVRON_HEIGHT).padTop(-3f);
        return plate;
    }

    public record Tab<T>(
            T value,
            String label,
            String activePlateId,
            String idlePlateId,
            String iconId) {
    }
}
