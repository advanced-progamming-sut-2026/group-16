package io.github.finalwave.view.gui.widget;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;


public final class PvzButtons {
    private static final String BROWN_BUTTON = "image_ui_generic_brownbutton_10";

    private PvzButtons() {
    }

    public static TextButton textButton(String text, Skin skin, String styleName, Runnable onClick) {
        TextButton button = new TextButton(text, skin, styleName);
        animate(button, 1.08f, 0.92f, onClick);
        return button;
    }


    public static Actor iconButton(TextureRegion icon, float width, float height, Runnable onClick) {
        Image image = new Image(new TextureRegionDrawable(icon));
        image.setScaling(Scaling.fit);
        Stack stack = new Stack();
        stack.setSize(width, height);
        stack.add(image);
        animate(stack, 1.1f, 0.9f, onClick);
        return stack;
    }


    public static Actor framedIconButton(Skin skin, TextureRegion icon, float size, Runnable onClick) {
        Stack stack = new Stack();
        stack.setSize(size, size);

        Drawable frame = null;
        if (skin.has(BROWN_BUTTON, Drawable.class)) {
            frame = skin.getDrawable(BROWN_BUTTON);
        } else if (skin.has("brown", TextButton.TextButtonStyle.class)) {
            frame = skin.get("brown", TextButton.TextButtonStyle.class).up;
        }
        if (frame != null) {
            Image background = new Image(frame);
            background.setScaling(Scaling.stretch);
            stack.add(background);
        }

        Image image = new Image(new TextureRegionDrawable(icon));
        image.setScaling(Scaling.fit);
        stack.add(image);
        animate(stack, 1.1f, 0.9f, onClick);
        return stack;
    }

    public static void animate(Actor actor, float hoverScale, float clickScale, Runnable onClick) {

        if (actor instanceof Group group) {
            group.setTransform(true);
        }
        actor.setOrigin(Align.center);

        actor.addListener(new ClickListener() {
            private boolean hovering;

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (pointer != -1) {
                    return;
                }

                if (fromActor != null && isWithin(actor, fromActor)) {
                    return;
                }
                hovering = true;
                actor.setOrigin(Align.center);
                actor.clearActions();
                actor.addAction(Actions.scaleTo(hoverScale, hoverScale, 0.12f, Interpolation.sineOut));
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (pointer != -1) {
                    return;
                }
                if (toActor != null && isWithin(actor, toActor)) {
                    return;
                }
                hovering = false;
                actor.clearActions();
                actor.addAction(Actions.scaleTo(1f, 1f, 0.12f, Interpolation.sineOut));
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                actor.setOrigin(Align.center);
                actor.clearActions();
                actor.addAction(Actions.scaleTo(clickScale, clickScale, 0.05f, Interpolation.sineOut));
                return super.touchDown(event, x, y, pointer, button);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);
                actor.clearActions();
                float target = hovering ? hoverScale : 1f;
                actor.addAction(Actions.scaleTo(target, target, 0.1f, Interpolation.sineOut));
            }

            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (onClick != null) {
                    onClick.run();
                }
            }
        });
    }

    private static boolean isWithin(Actor root, Actor other) {
        return other == root || other.isDescendantOf(root);
    }
}
