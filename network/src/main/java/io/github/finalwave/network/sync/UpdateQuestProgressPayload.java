package io.github.finalwave.network.sync;

import java.util.ArrayList;
import java.util.List;

public final class UpdateQuestProgressPayload {
    private List<QuestProgressRow> rows = new ArrayList<>();

    public UpdateQuestProgressPayload() {
    }

    public List<QuestProgressRow> getRows() {
        return rows;
    }

    public void setRows(List<QuestProgressRow> rows) {
        this.rows = rows == null ? new ArrayList<>() : rows;
    }

    public static final class QuestProgressRow {
        private String questId;
        private boolean completed;
        private boolean claimed;
        private String progressBlob;

        public QuestProgressRow() {
        }

        public String getQuestId() {
            return questId;
        }

        public void setQuestId(String questId) {
            this.questId = questId;
        }

        public boolean isCompleted() {
            return completed;
        }

        public void setCompleted(boolean completed) {
            this.completed = completed;
        }

        public boolean isClaimed() {
            return claimed;
        }

        public void setClaimed(boolean claimed) {
            this.claimed = claimed;
        }

        public String getProgressBlob() {
            return progressBlob;
        }

        public void setProgressBlob(String progressBlob) {
            this.progressBlob = progressBlob;
        }
    }
}
