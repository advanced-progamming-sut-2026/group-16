package io.github.finalwave.view.gui.widget;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;

public final class StickerAtlasActor extends Image {
    private final Animation<TextureRegion> animation;
    private final TextureRegionDrawable drawable = new TextureRegionDrawable();
    private float stateTime;

    public StickerAtlasActor(Animation<TextureRegion> animation) {
        this.animation = animation;
        setScaling(Scaling.fit);
        TextureRegion first = animation == null ? null : animation.getKeyFrame(0f, true);
        if (first != null) {
            drawable.setRegion(first);
            setDrawable(drawable);
        }
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (animation == null) {
            return;
        }
        stateTime += delta;
        TextureRegion frame = animation.getKeyFrame(stateTime, true);
        if (frame == null) {
            return;
        }
        drawable.setRegion(frame);
        setDrawable(drawable);
    }
}
