package io.github.finalwave.network.sync;

public final class UpdateSettingsPayload {
    private int gameSpeed;
    private boolean showLawnGrid;
    private boolean debugMode;

    public UpdateSettingsPayload() {
    }

    public int getGameSpeed() {
        return gameSpeed;
    }

    public void setGameSpeed(int gameSpeed) {
        this.gameSpeed = gameSpeed;
    }

    public boolean isShowLawnGrid() {
        return showLawnGrid;
    }

    public void setShowLawnGrid(boolean showLawnGrid) {
        this.showLawnGrid = showLawnGrid;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }
}
