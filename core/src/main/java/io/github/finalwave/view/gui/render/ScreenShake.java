package io.github.finalwave.view.gui.render;

import java.util.concurrent.ThreadLocalRandom;

public final class ScreenShake {
    private float remaining;
    private float duration;
    private float magnitude;
    private float offsetX;
    private float offsetY;

    public void trigger(float seconds, float pixels) {
        if (seconds <= 0f || pixels <= 0f) {
            return;
        }
        if (pixels >= magnitude || remaining <= 0f) {
            duration = seconds;
            remaining = seconds;
            magnitude = pixels;
        }
    }

    public void update(float delta) {
        if (remaining <= 0f) {
            offsetX = 0f;
            offsetY = 0f;
            magnitude = 0f;
            return;
        }
        remaining = Math.max(0f, remaining - Math.max(0f, delta));
        float strength = duration <= 0f ? 0f : (remaining / duration) * magnitude;
        ThreadLocalRandom random = ThreadLocalRandom.current();
        offsetX = (random.nextFloat() * 2f - 1f) * strength;
        offsetY = (random.nextFloat() * 2f - 1f) * strength;
        if (remaining <= 0f) {
            offsetX = 0f;
            offsetY = 0f;
            magnitude = 0f;
        }
    }

    public float offsetX() {
        return offsetX;
    }

    public float offsetY() {
        return offsetY;
    }

    public void reset() {
        remaining = 0f;
        duration = 0f;
        magnitude = 0f;
        offsetX = 0f;
        offsetY = 0f;
    }
}
