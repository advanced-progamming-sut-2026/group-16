package io.github.finalwave.model.game.board;

import io.github.finalwave.model.game.entity.plant.Plant;

public final class PlantCell {

    private Plant ground;
    private Plant overlay;

    public Plant getGround() {
        return ground;
    }

    public Plant getOverlay() {
        return overlay;
    }

    public void setGround(Plant ground) {
        this.ground = ground;
    }

    public void setOverlay(Plant overlay) {
        this.overlay = overlay;
    }

    public boolean isEmpty() {
        return ground == null && overlay == null;
    }

    public Plant primaryPlant() {
        return overlay != null ? overlay : ground;
    }

    public Plant plantInFront(double zombieX) {
        int col = (int) Math.floor(zombieX);
        if (overlay != null && overlay.getCol() == col && overlay.isAlive()) {
            return overlay;
        }
        if (ground != null && ground.getCol() == col && ground.isAlive()) {
            return ground;
        }
        if (overlay != null && overlay.isAlive()) {
            return overlay;
        }
        return ground != null && ground.isAlive() ? ground : null;
    }

    public void remove(Plant plant) {
        if (ground == plant) {
            ground = null;
        }
        if (overlay == plant) {
            overlay = null;
        }
    }
}
