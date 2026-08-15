package io.github.finalwave.view.gui.widget;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import io.github.finalwave.view.gui.assets.CollectionCardLooks;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.assets.MenuAssetIds;


public final class MintFamilyBadge extends Group {
    private static final float ICON_INSET = 0.22f;

    private final GameAssets assets;
    private final Image banner;
    private final Image icon;

    public MintFamilyBadge(GameAssets assets) {
        this.assets = assets;
        setTouchable(Touchable.disabled);
        banner = new Image(new TextureRegionDrawable(assets.region(MenuAssetIds.MINTFAM_BANNER)));
        banner.setScaling(Scaling.fit);
        banner.setTouchable(Touchable.disabled);
        icon = new Image();
        icon.setScaling(Scaling.fit);
        icon.setColor(Color.WHITE);
        icon.setTouchable(Touchable.disabled);
        addActor(banner);
        addActor(icon);
    }

    public void bind(String category) {
        String iconId = CollectionCardLooks.familyIcon(category);
        if (!assets.hasImage(MenuAssetIds.MINTFAM_BANNER) || !assets.hasImage(iconId)) {
            setVisible(false);
            return;
        }
        setVisible(true);
        banner.setColor(CollectionCardLooks.familyTint(category));
        icon.setDrawable(new TextureRegionDrawable(assets.region(iconId)));
        layoutPieces();
    }

    @Override
    protected void sizeChanged() {
        super.sizeChanged();
        layoutPieces();
    }

    private void layoutPieces() {
        float size = Math.min(getWidth(), getHeight());
        banner.setBounds(0f, 0f, size, size);
        float inset = size * ICON_INSET;
        icon.setBounds(inset, inset, size - inset * 2f, size - inset * 2f);
    }
}
