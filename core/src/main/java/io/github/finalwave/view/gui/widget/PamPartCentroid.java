package io.github.finalwave.view.gui.widget;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import pvz.libpvz.pam.PamPlayer;


public final class PamPartCentroid {
    private PamPartCentroid() {
    }

    public static Vector2 of(PamPlayer player, String pam, String clip, String part) {
        if (player == null || pam == null || clip == null || part == null) {
            return new Vector2();
        }
        player.loadSync(pam);
        CapturingBatch batch = new CapturingBatch();
        try {
            player.drawPart(batch, pam, clip, 0f, 0f, 0f, part);
            return batch.centroid();
        } finally {
            batch.dispose();
        }
    }

    private static final class CapturingBatch extends SpriteBatch {
        private float sumX;
        private float sumY;
        private int vertices;

        @Override
        public void draw(Texture texture, float[] spriteVertices, int offset, int count) {
            int end = offset + count;
            for (int i = offset; i < end; i += 5) {
                sumX += spriteVertices[i];
                sumY += spriteVertices[i + 1];
                vertices++;
            }
        }

        Vector2 centroid() {
            if (vertices == 0) {
                return new Vector2();
            }
            return new Vector2(sumX / vertices, sumY / vertices);
        }
    }
}
