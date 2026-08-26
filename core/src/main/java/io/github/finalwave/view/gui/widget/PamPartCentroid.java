package io.github.finalwave.view.gui.widget;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import pvz.libpvz.pam.PamPlayer;


public final class PamPartCentroid {
    private static CapturingBatch capture;
    private static final Vector2 TMP = new Vector2();

    private PamPartCentroid() {
    }

    public static Vector2 of(PamPlayer player, String pam, String clip, String part) {
        return at(player, pam, clip, part, 0f, 0f, 0f, TMP);
    }

    public static Vector2 at(PamPlayer player,
                             String pam,
                             String clip,
                             String part,
                             float time,
                             float x,
                             float y,
                             Vector2 out) {
        if (out == null) {
            out = new Vector2();
        }
        out.setZero();
        if (player == null || pam == null || clip == null || part == null) {
            return out;
        }
        player.loadSync(pam);
        CapturingBatch batch = batch();
        batch.reset();
        batch.begin();
        try {
            player.drawPart(batch, pam, clip, time, x, y, part);
        } finally {
            if (batch.isDrawing()) {
                batch.end();
            }
        }
        return batch.centroid(out);
    }

    private static CapturingBatch batch() {
        if (capture == null) {
            capture = new CapturingBatch();
        }
        return capture;
    }

    private static final class CapturingBatch extends SpriteBatch {
        private float sumX;
        private float sumY;
        private int vertices;

        void reset() {
            sumX = 0f;
            sumY = 0f;
            vertices = 0;
        }

        @Override
        public void draw(Texture texture, float[] spriteVertices, int offset, int count) {
            int end = offset + count;
            for (int i = offset; i < end; i += 5) {
                sumX += spriteVertices[i];
                sumY += spriteVertices[i + 1];
                vertices++;
            }
        }

        Vector2 centroid(Vector2 out) {
            if (vertices == 0) {
                return out.setZero();
            }
            return out.set(sumX / vertices, sumY / vertices);
        }
    }
}
