package io.github.finalwave.view.gui.input;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import io.github.finalwave.controller.CouchIZombieController;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.view.gui.hud.CouchPickOverlay;

import java.util.List;
import java.util.function.BooleanSupplier;

public final class CouchZombieKeys extends InputAdapter {
    private static final int INVALID = -1;

    private final CouchIZombieController controller;
    private final BooleanSupplier blocked;
    private CouchPickOverlay pickOverlay;
    private int cursorCol = INVALID;
    private int cursorRow = INVALID;
    private int rosterIndex;
    private String selectedAlias;
    private boolean active;

    public CouchZombieKeys(CouchIZombieController controller, BooleanSupplier blocked) {
        this.controller = controller;
        this.blocked = blocked;
    }

    public void setPickOverlay(CouchPickOverlay pickOverlay) {
        this.pickOverlay = pickOverlay;
    }

    public void activate() {
        active = true;
        if (controller.isPicking()) {
            return;
        }
        resetCursor();
    }

    public void deactivate() {
        active = false;
        selectedAlias = null;
    }

    public boolean isActive() {
        return active;
    }

    public int cursorCol() {
        return cursorCol;
    }

    public int cursorRow() {
        return cursorRow;
    }

    public String selectedAlias() {
        return selectedAlias;
    }

    public void resetCursor() {
        GameSession session = controller.session();
        if (session == null) {
            return;
        }
        cursorCol = session.getIZombiePlacementColumn() + 1;
        cursorRow = session.getBoard().getRows() / 2;
        rosterIndex = 0;
        List<String> pool = controller.session().getIZombieZombiePool();
        selectedAlias = pool.isEmpty() ? null : pool.getFirst();
    }

    @Override
    public boolean keyDown(int keycode) {
        if (!active || blocked.getAsBoolean()) {
            return false;
        }
        if (controller.isPicking()) {
            return handlePicking(keycode);
        }
        return handlePlaying(keycode);
    }

    private boolean handlePicking(int keycode) {
        CouchPickOverlay overlay = pickOverlay;
        if (overlay == null) {
            return false;
        }
        switch (keycode) {
            case Input.Keys.UP, Input.Keys.LEFT -> {
                overlay.moveZombieFocus(-1);
                return true;
            }
            case Input.Keys.DOWN, Input.Keys.RIGHT, Input.Keys.TAB -> {
                overlay.moveZombieFocus(1);
                return true;
            }
            case Input.Keys.SPACE -> {
                overlay.toggleZombieFocused();
                return true;
            }
            case Input.Keys.ENTER -> {
                overlay.submitZombie();
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    private boolean handlePlaying(int keycode) {
        GameSession session = controller.session();
        if (session == null) {
            return false;
        }
        switch (keycode) {
            case Input.Keys.UP, Input.Keys.W -> {
                moveCursor(0, -1);
                return true;
            }
            case Input.Keys.DOWN, Input.Keys.S -> {
                moveCursor(0, 1);
                return true;
            }
            case Input.Keys.LEFT, Input.Keys.A -> {
                moveCursor(-1, 0);
                return true;
            }
            case Input.Keys.RIGHT, Input.Keys.D -> {
                moveCursor(1, 0);
                return true;
            }
            case Input.Keys.TAB, Input.Keys.Q -> {
                cycleRoster(1);
                return true;
            }
            case Input.Keys.E -> {
                cycleRoster(-1);
                return true;
            }
            case Input.Keys.SPACE, Input.Keys.ENTER -> {
                dropSelected();
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    private void moveCursor(int dCol, int dRow) {
        GameSession session = controller.session();
        if (session == null) {
            return;
        }
        int minCol = session.getIZombiePlacementColumn() + 1;
        int maxCol = session.getBoard().getCols() - 1;
        int minRow = 0;
        int maxRow = session.getBoard().getRows() - 1;
        if (cursorCol == INVALID) {
            resetCursor();
            return;
        }
        cursorCol = Math.max(minCol, Math.min(maxCol, cursorCol + dCol));
        cursorRow = Math.max(minRow, Math.min(maxRow, cursorRow + dRow));
    }

    private void cycleRoster(int delta) {
        List<String> pool = controller.session().getIZombieZombiePool();
        if (pool.isEmpty()) {
            return;
        }
        rosterIndex = Math.floorMod(rosterIndex + delta, pool.size());
        selectedAlias = pool.get(rosterIndex);
    }

    public void selectAlias(String alias) {
        List<String> pool = controller.session().getIZombieZombiePool();
        int index = pool.indexOf(alias);
        if (index >= 0) {
            rosterIndex = index;
            selectedAlias = alias;
        }
    }

    private void dropSelected() {
        if (cursorCol < 0 || cursorRow < 0 || selectedAlias == null) {
            return;
        }
        controller.placeZombie(selectedAlias, cursorCol, cursorRow);
    }
}
