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

    public static Slider create(Skin skin, int min, int max) {
        if (max < min) {
            throw new IllegalArgumentException("slider max must be >= min");
        }
        Slider slider = new Slider(min, max, 1f, false, buildStyle(skin));
        slider.setValue(min);
        return slider;
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
