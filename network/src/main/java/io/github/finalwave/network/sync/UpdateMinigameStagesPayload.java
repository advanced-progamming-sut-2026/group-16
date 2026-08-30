package io.github.finalwave.network.sync;

import java.util.ArrayList;
import java.util.List;

public final class UpdateMinigameStagesPayload {
    private List<MinigameStageRow> rows = new ArrayList<>();

    public UpdateMinigameStagesPayload() {
    }

    public List<MinigameStageRow> getRows() {
        return rows;
    }

    public void setRows(List<MinigameStageRow> rows) {
        this.rows = rows == null ? new ArrayList<>() : rows;
    }

    public static final class MinigameStageRow {
        private String minigameId;
        private int stageIndex;

        public MinigameStageRow() {
        }

        public String getMinigameId() {
            return minigameId;
        }

        public void setMinigameId(String minigameId) {
            this.minigameId = minigameId;
        }

        public int getStageIndex() {
            return stageIndex;
        }

        public void setStageIndex(int stageIndex) {
            this.stageIndex = stageIndex;
        }
    }
}
