package io.github.finalwave.view.gui.widget;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.GdxRuntimeException;

public final class ThemedSlider {
    private static final String[] KNOB_CANDIDATES = {
            "image_ui_mainmenu_slider_bolt",
            "image_ui_almanac_scroll_slider"
    };
    private static final String[] BACKGROUND_CANDIDATES = {
            "image_ui_almanac_plants_plant_fuelbar_10",
            "image_ui_almanac_plants_plant_fuelbar",
            "image_ui_almanac_zombies_zombie_fuelbar_10",
            "image_ui_almanac_zombies_zombie_fuelbar"
    };
    private static final String[] FILL_CANDIDATES = {
            "image_ui_almanac_general_fuelbar_fill_10",
            "image_ui_almanac_general_fuelbar_fill"
    };

    private ThemedSlider() {
    }

    public static Slider createVolume(Skin skin, TextureRegion knob) {
        Slider.SliderStyle style = new Slider.SliderStyle();
        style.background = StoreChrome.volumeTrack();
        style.knobBefore = null;
        style.knobAfter = null;
        Drawable gear = knobDrawable(skin, knob);
        style.knob = gear;
        style.knobOver = gear;
        style.knobDown = gear;
        Slider slider = new Slider(0f, 100f, 1f, false, style);
        slider.setValue(100f);
        return slider;
    }

    private static Drawable knobDrawable(Skin skin, TextureRegion knob) {
        if (knob != null) {
            TextureRegionDrawable gear = new TextureRegionDrawable(knob);
            float size = Math.max(knob.getRegionWidth(), knob.getRegionHeight());
            gear.setMinWidth(size);
            gear.setMinHeight(size);
            return gear;
        }
        return firstDrawable(skin, KNOB_CANDIDATES);
    }

    public static Slider create(Skin skin, int min, int max) {
        return create(skin, min, max, 1f);
    }

    public static Slider create(Skin skin, float min, float max, float step) {
        if (max < min) {
            throw new IllegalArgumentException("slider max must be >= min");
        }
        Slider slider = new Slider(min, max, step, false, buildStyle(skin));
        slider.setValue(min);
        return slider;
    }

    public static void applyKnob(Slider slider, TextureRegion knob) {
        if (slider == null || knob == null) {
            return;
        }
        Slider.SliderStyle style = new Slider.SliderStyle(slider.getStyle());
        Drawable drawable = new TextureRegionDrawable(knob);
        style.knob = drawable;
        style.knobOver = drawable;
        style.knobDown = drawable;
        slider.setStyle(style);
    }

    public static Slider.SliderStyle buildStyle(Skin skin) {
        Slider.SliderStyle style;
        if (skin.has("default-horizontal", Slider.SliderStyle.class)) {
            style = new Slider.SliderStyle(skin.get("default-horizontal", Slider.SliderStyle.class));
        } else {
            style = new Slider.SliderStyle();
        }
        if (style.background == null) {
            style.background = firstDrawable(skin, BACKGROUND_CANDIDATES);
        }
        if (style.knobBefore == null) {
            style.knobBefore = firstDrawable(skin, FILL_CANDIDATES);
        }
        Drawable knob = firstDrawable(skin, KNOB_CANDIDATES);
        if (knob != null) {
            style.knob = knob;
            style.knobOver = knob;
            style.knobDown = knob;
        }
        return style;
    }

    private static Drawable firstDrawable(Skin skin, String... names) {
        for (String name : names) {
            Drawable drawable = findDrawable(skin, name);
            if (drawable != null) {
                return drawable;
            }
        }
        return null;
    }

    private static Drawable findDrawable(Skin skin, String name) {
        if (skin.has(name, Drawable.class)) {
            return skin.getDrawable(name);
        }
        if (skin.has(name, TextureRegion.class)) {
            return new TextureRegionDrawable(skin.getRegion(name));
        }
        TextureAtlas atlas = skin.getAtlas();
        if (atlas != null) {
            TextureRegion region = atlas.findRegion(name);
            if (region != null) {
                return new TextureRegionDrawable(region);
            }
        }
        try {
            return skin.getDrawable(name);
        } catch (GdxRuntimeException ignored) {
            return null;
        }
    }
}
