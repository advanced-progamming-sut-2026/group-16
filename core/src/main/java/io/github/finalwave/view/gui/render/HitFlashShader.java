package io.github.finalwave.view.gui.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Disposable;


public final class HitFlashShader implements Disposable {
    private static final String TAG = "HitFlashShader";

    private final ShaderProgram program;

    public HitFlashShader() {
        ShaderProgram.pedantic = false;
        this.program = new ShaderProgram(
                Gdx.files.classpath("shaders/hit_flash.vert"),
                Gdx.files.classpath("shaders/hit_flash.frag"));
        if (!program.isCompiled()) {
            Gdx.app.error(TAG, program.getLog());
        }
    }

    public boolean isReady() {
        return program.isCompiled();
    }

    public void bind(Batch batch, float flash01) {
        if (batch == null || !program.isCompiled()) {
            return;
        }
        batch.setShader(program);
        program.bind();
        program.setUniformf("u_damageFlash", Math.max(0f, Math.min(1f, flash01)));
    }

    public void unbind(Batch batch) {
        if (batch == null) {
            return;
        }
        batch.setShader(null);
    }

    @Override
    public void dispose() {
        program.dispose();
    }
}
