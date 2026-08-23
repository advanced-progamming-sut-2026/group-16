package io.github.finalwave.view.gui.render.sync;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import io.github.finalwave.controller.BeghouledController;
import io.github.finalwave.model.game.GameSession;
import io.github.finalwave.model.minigame.beghouled.BeghouledGrid;
import io.github.finalwave.model.minigame.beghouled.BeghouledSwapOutcome;
import io.github.finalwave.model.minigame.beghouled.BeghouledSwapResult;
import io.github.finalwave.view.gui.assets.EntityAnimationCatalog;
import io.github.finalwave.view.gui.assets.GameAssets;
import io.github.finalwave.view.gui.render.LawnLayout;
import io.github.finalwave.view.gui.render.clip.PlantClips;
import io.github.finalwave.view.gui.widget.PamActor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BooleanSupplier;


public final class BeghouledPlantSync {
    private static final float SWAP_SECONDS = 0.18f;
    private static final float FADE_SECONDS = 0.14f;
    private static final float FALL_SECONDS = 0.22f;

    private final GameAssets assets;
    private final LawnLayout layout;
    private final PlantClips clips;
    private final Group layer;
    private final Actor inputTarget;
    private final InputListener listener;

    private BeghouledController controller;
    private BooleanSupplier blocked;
    private PamActor[][] actors = new PamActor[0][0];
    private String[][] names = new String[0][0];
    private boolean busy;
    private boolean swapOverlap;
    private int pending;
    private int dragCol = -1;
    private int dragRow = -1;

    public BeghouledPlantSync(GameAssets assets,
                              LawnLayout layout,
                              PlantClips clips,
                              Group layer,
                              Actor inputTarget) {
        this.assets = assets;
        this.layout = layout;
        this.clips = clips;
        this.layer = layer;
        this.inputTarget = inputTarget;
        this.listener = new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return BeghouledPlantSync.this.touchDown(x, y, button);
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                BeghouledPlantSync.this.touchDragged(x, y);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                BeghouledPlantSync.this.touchUp(x, y);
            }
        };
        if (inputTarget != null) {
            inputTarget.addListener(listener);
        }
    }

    public void setController(BeghouledController controller, BooleanSupplier blocked) {
        this.controller = controller;
        this.blocked = blocked;
    }

    public boolean isBusy() {
        return busy;
    }

    public boolean holdsSwapOverlap() {
        return swapOverlap;
    }

    public void sync(GameSession session) {
        if (session == null || !session.isBeghouledActive() || session.getBeghouledBoard() == null
                || session.getBeghouledBoard().getGrid() == null) {
            clear();
            return;
        }
        if (busy) {
            return;
        }
        snapToGrid(session.getBeghouledBoard().getGrid());
    }

    public void clear() {
        busy = false;
        swapOverlap = false;
        pending = 0;
        dragCol = -1;
        dragRow = -1;
        for (int row = 0; row < actors.length; row++) {
            for (int col = 0; col < actors[row].length; col++) {
                removeCell(col, row);
            }
        }
        actors = new PamActor[0][0];
        names = new String[0][0];
    }

    public void dispose() {
        if (inputTarget != null && listener != null) {
            inputTarget.removeListener(listener);
        }
        clear();
    }

    private boolean touchDown(float x, float y, int button) {
        if (button != Input.Buttons.LEFT || !canDrag()) {
            return false;
        }
        int col = layout.colAt(x);
        int row = layout.rowAt(y);
        if (!hasPlant(col, row)) {
            return false;
        }
        dragCol = col;
        dragRow = row;
        return true;
    }

    private void touchDragged(float x, float y) {
        if (dragCol < 0 || dragRow < 0 || !canDrag()) {
            return;
        }
        int col = layout.colAt(x);
        int row = layout.rowAt(y);
        if (!adjacent(dragCol, dragRow, col, row) || !hasPlant(col, row)) {
            return;
        }
        int startCol = dragCol;
        int startRow = dragRow;
        dragCol = -1;
        dragRow = -1;
        beginSwap(startCol, startRow, col, row);
    }

    private void touchUp(float x, float y) {
        int startCol = dragCol;
        int startRow = dragRow;
        dragCol = -1;
        dragRow = -1;
        if (startCol < 0 || startRow < 0 || !canDrag()) {
            return;
        }
        int col = layout.colAt(x);
        int row = layout.rowAt(y);
        if (!adjacent(startCol, startRow, col, row) || !hasPlant(col, row)) {
            return;
        }
        beginSwap(startCol, startRow, col, row);
    }

    private boolean canDrag() {
        if (busy || controller == null) {
            return false;
        }
        return blocked == null || !blocked.getAsBoolean();
    }

    private boolean hasPlant(int col, int row) {
        return inBounds(col, row) && actors[row][col] != null && names[row][col] != null;
    }

    private boolean adjacent(int colA, int rowA, int colB, int rowB) {
        return Math.abs(colA - colB) + Math.abs(rowA - rowB) == 1;
    }

    private void beginSwap(int colA, int rowA, int colB, int rowB) {
        PamActor actorA = actors[rowA][colA];
        PamActor actorB = actors[rowB][colB];
        if (actorA == null || actorB == null) {
            return;
        }
        busy = true;
        swapOverlap = true;
        pending = 1;
        actorA.clearActions();
        actorB.clearActions();
        if (rowA != rowB) {
            PamActor upper = rowA < rowB ? actorA : actorB;
            upper.toFront();
        } else {
            actorA.toFront();
            actorB.toFront();
        }
        Vector2 destA = pose(colB, rowB);
        Vector2 destB = pose(colA, rowA);
        actorA.addAction(Actions.moveTo(destA.x, destA.y, SWAP_SECONDS, Interpolation.sine));
        actorB.addAction(Actions.sequence(
                Actions.moveTo(destB.x, destB.y, SWAP_SECONDS, Interpolation.sine),
                Actions.run(() -> afterSwapTween(colA, rowA, colB, rowB, actorA, actorB))));
    }

    private void afterSwapTween(int colA, int rowA, int colB, int rowB, PamActor actorA, PamActor actorB) {
        swapOverlap = false;
        BeghouledSwapResult result = controller.swapPlants(colA, rowA, colB, rowB);
        if (result == null || result.outcome() != BeghouledSwapOutcome.SUCCESS) {
            Vector2 backA = pose(colA, rowA);
            Vector2 backB = pose(colB, rowB);
            actorA.clearActions();
            actorB.clearActions();
            actorA.addAction(Actions.moveTo(backA.x, backA.y, SWAP_SECONDS, Interpolation.sine));
            actorB.addAction(Actions.sequence(
                    Actions.moveTo(backB.x, backB.y, SWAP_SECONDS, Interpolation.sine),
                    Actions.run(this::finishWork)));
            return;
        }
        swapSlots(colA, rowA, colB, rowB);
        if (result.boardReset()) {
            refillBoard();
            return;
        }
        animateCascade();
    }

    private void swapSlots(int colA, int rowA, int colB, int rowB) {
        PamActor actor = actors[rowA][colA];
        actors[rowA][colA] = actors[rowB][colB];
        actors[rowB][colB] = actor;
        String name = names[rowA][colA];
        names[rowA][colA] = names[rowB][colB];
        names[rowB][colB] = name;
        if (actors[rowA][colA] != null) {
            actors[rowA][colA].setUserObject(rowA);
        }
        if (actors[rowB][colB] != null) {
            actors[rowB][colB].setUserObject(rowB);
        }
    }

    private void animateCascade() {
        if (controller == null || controller.session() == null || controller.session().getBeghouledBoard() == null) {
            finishWork();
            return;
        }
        BeghouledGrid grid = controller.session().getBeghouledBoard().getGrid();
        if (grid == null) {
            finishWork();
            return;
        }
        pending = 0;
        int rows = grid.getRows();
        int cols = grid.getCols();
        ensureSize(rows, cols);
        for (int col = 0; col < cols; col++) {
            animateColumn(grid, col);
        }
        sortLayerByRow();
        if (pending <= 0) {
            finishWork();
        }
    }

    private void animateColumn(BeghouledGrid grid, int col) {
        int rows = grid.getRows();
        List<PamActor> poolActors = new ArrayList<>();
        List<String> poolNames = new ArrayList<>();
        for (int row = rows - 1; row >= 0; row--) {
            if (grid.isCrater(col, row)) {
                removeCell(col, row);
                continue;
            }
            if (actors[row][col] != null && names[row][col] != null) {
                poolActors.add(actors[row][col]);
                poolNames.add(names[row][col]);
                actors[row][col] = null;
                names[row][col] = null;
            }
        }
        boolean[] used = new boolean[poolActors.size()];
        int spawnStack = 0;
        for (int row = rows - 1; row >= 0; row--) {
            if (grid.isCrater(col, row)) {
                continue;
            }
            String want = grid.getPlant(col, row);
            if (want == null) {
                continue;
            }
            int found = -1;
            for (int i = 0; i < poolActors.size(); i++) {
                if (!used[i] && want.equals(poolNames.get(i))) {
                    found = i;
                    break;
                }
            }
            if (found >= 0) {
                used[found] = true;
                PamActor actor = poolActors.get(found);
                assign(actor, want, col, row);
                Vector2 dest = pose(col, row);
                if (near(actor, dest)) {
                    actor.setPosition(dest.x, dest.y);
                } else {
                    enqueueMove(actor, dest);
                }
            } else {
                spawnStack++;
                PamActor spawned = spawn(want);
                Vector2 dest = pose(col, row);
                Vector2 from = spawnAbove(col, spawnStack);
                spawned.setPosition(from.x, from.y);
                assign(spawned, want, col, row);
                enqueueMove(spawned, dest);
            }
        }
        for (int i = 0; i < poolActors.size(); i++) {
            if (!used[i]) {
                enqueueFade(poolActors.get(i));
            }
        }
    }

    private void refillBoard() {
        if (controller == null || controller.session() == null || controller.session().getBeghouledBoard() == null) {
            finishWork();
            return;
        }
        BeghouledGrid grid = controller.session().getBeghouledBoard().getGrid();
        if (grid == null) {
            finishWork();
            return;
        }
        pending = 0;
        int rows = grid.getRows();
        int cols = grid.getCols();
        ensureSize(rows, cols);
        List<PamActor> old = new ArrayList<>();
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (actors[row][col] != null) {
                    old.add(actors[row][col]);
                    actors[row][col] = null;
                    names[row][col] = null;
                }
            }
        }
        for (PamActor actor : old) {
            enqueueFade(actor);
        }
        int[] stacks = new int[cols];
        for (int row = rows - 1; row >= 0; row--) {
            for (int col = 0; col < cols; col++) {
                if (grid.isCrater(col, row)) {
                    continue;
                }
                String want = grid.getPlant(col, row);
                if (want == null) {
                    continue;
                }
                stacks[col]++;
                PamActor spawned = spawn(want);
                Vector2 dest = pose(col, row);
                Vector2 from = spawnAbove(col, stacks[col]);
                spawned.setPosition(from.x, from.y);
                assign(spawned, want, col, row);
                enqueueMove(spawned, dest);
            }
        }
        sortLayerByRow();
        if (pending <= 0) {
            finishWork();
        }
    }

    private void snapToGrid(BeghouledGrid grid) {
        int rows = grid.getRows();
        int cols = grid.getCols();
        ensureSize(rows, cols);
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (grid.isCrater(col, row) || grid.getPlant(col, row) == null) {
                    removeCell(col, row);
                    continue;
                }
                String want = grid.getPlant(col, row);
                PamActor actor = actors[row][col];
                if (actor == null) {
                    actor = spawn(want);
                    assign(actor, want, col, row);
                    applyPose(actor, col, row);
                    continue;
                }
                if (!want.equals(names[row][col])) {
                    applyClip(actor, want);
                    names[row][col] = want;
                }
                actor.clearActions();
                actor.getColor().a = 1f;
                applyPose(actor, col, row);
            }
        }
        sortLayerByRow();
    }

    private void enqueueMove(PamActor actor, Vector2 dest) {
        pending++;
        actor.clearActions();
        actor.addAction(Actions.sequence(
                Actions.moveTo(dest.x, dest.y, FALL_SECONDS, Interpolation.sine),
                Actions.run(this::completeOne)));
    }

    private void enqueueFade(PamActor actor) {
        pending++;
        actor.clearActions();
        actor.toFront();
        actor.addAction(Actions.sequence(
                Actions.fadeOut(FADE_SECONDS, Interpolation.sine),
                Actions.run(this::completeOne),
                Actions.removeActor()));
    }

    private void completeOne() {
        pending--;
        if (pending == 0) {
            finishWork();
        }
    }

    private void finishWork() {
        pending = 0;
        busy = false;
        swapOverlap = false;
        if (controller != null && controller.session() != null
                && controller.session().getBeghouledBoard() != null
                && controller.session().getBeghouledBoard().getGrid() != null) {
            snapToGrid(controller.session().getBeghouledBoard().getGrid());
        }
    }

    private void ensureSize(int rows, int cols) {
        if (actors.length == rows && (rows == 0 || actors[0].length == cols)) {
            return;
        }
        PamActor[][] nextActors = new PamActor[rows][cols];
        String[][] nextNames = new String[rows][cols];
        int copyRows = Math.min(rows, actors.length);
        int copyCols = actors.length == 0 ? 0 : Math.min(cols, actors[0].length);
        for (int row = 0; row < copyRows; row++) {
            System.arraycopy(actors[row], 0, nextActors[row], 0, copyCols);
            System.arraycopy(names[row], 0, nextNames[row], 0, copyCols);
        }
        for (int row = 0; row < actors.length; row++) {
            for (int col = copyCols; col < actors[row].length; col++) {
                if (actors[row][col] != null) {
                    actors[row][col].remove();
                }
            }
        }
        for (int row = copyRows; row < actors.length; row++) {
            for (int col = 0; col < copyCols; col++) {
                if (actors[row][col] != null) {
                    actors[row][col].remove();
                }
            }
        }
        actors = nextActors;
        names = nextNames;
    }

    private PamActor spawn(String plantName) {
        PamActor actor = assets.pamActor();
        actor.setTouchable(Touchable.disabled);
        actor.setAnchor(0.5f, LawnLayout.PLANT_ANCHOR_Y);
        actor.setSize(layout.tileWidth(), layout.tileHeight());
        actor.setColor(Color.WHITE);
        applyClip(actor, plantName);
        layer.addActor(actor);
        return actor;
    }

    private void applyClip(PamActor actor, String plantName) {
        EntityAnimationCatalog.ClipSpec idle = clips.idle(plantName);
        actor.setClip(idle.path(), idle.clip(), clips.scale(plantName), true);
    }

    private void assign(PamActor actor, String plantName, int col, int row) {
        actors[row][col] = actor;
        names[row][col] = plantName;
        actor.setUserObject(row);
        actor.setSize(layout.tileWidth(), layout.tileHeight());
    }

    private void applyPose(PamActor actor, int col, int row) {
        Vector2 dest = pose(col, row);
        actor.setSize(layout.tileWidth(), layout.tileHeight());
        actor.setPosition(dest.x, dest.y);
        actor.setUserObject(row);
    }

    private void sortLayerByRow() {
        layer.getChildren().sort(Comparator.comparingInt(BeghouledPlantSync::rowOf));
    }

    private static int rowOf(Actor actor) {
        Object key = actor.getUserObject();
        return key instanceof Integer value ? value : 0;
    }

    private Vector2 pose(int col, int row) {
        Vector2 center = layout.cellCenter(col, row);
        return new Vector2(center.x - layout.tileWidth() / 2f, center.y - layout.tileHeight() / 2f);
    }

    private Vector2 spawnAbove(int col, int stack) {
        Vector2 dest = pose(col, 0);
        return new Vector2(dest.x, dest.y + stack * layout.tileHeight());
    }

    private void removeCell(int col, int row) {
        if (!inBounds(col, row)) {
            return;
        }
        PamActor actor = actors[row][col];
        if (actor != null) {
            actor.clearActions();
            actor.remove();
        }
        actors[row][col] = null;
        names[row][col] = null;
    }

    private boolean inBounds(int col, int row) {
        return row >= 0 && row < actors.length && col >= 0 && col < actors[row].length;
    }

    private static boolean near(Actor actor, Vector2 dest) {
        return Math.abs(actor.getX() - dest.x) < 1f && Math.abs(actor.getY() - dest.y) < 1f;
    }
}
