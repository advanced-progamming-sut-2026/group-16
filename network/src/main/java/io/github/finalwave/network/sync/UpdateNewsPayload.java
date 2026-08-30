package io.github.finalwave.network.sync;

import java.util.ArrayList;
import java.util.List;

public final class UpdateNewsPayload {
    private List<NewsRow> rows = new ArrayList<>();

    public UpdateNewsPayload() {
    }

    public List<NewsRow> getRows() {
        return rows;
    }

    public void setRows(List<NewsRow> rows) {
        this.rows = rows == null ? new ArrayList<>() : rows;
    }

    public static final class NewsRow {
        private long id;
        private String type;
        private String subject;
        private String message;
        private long createdAtMillis;
        private boolean read;

        public NewsRow() {
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public long getCreatedAtMillis() {
            return createdAtMillis;
        }

        public void setCreatedAtMillis(long createdAtMillis) {
            this.createdAtMillis = createdAtMillis;
        }

        public boolean isRead() {
            return read;
        }

        public void setRead(boolean read) {
            this.read = read;
        }
    }
}
