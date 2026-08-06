package io.github.finalwave.model.user;

public class NewsItem {
    private long id;
    private NewsType type;
    private String subject;
    private String message;
    private long createdAtMillis;
    private boolean read;

    public NewsItem() {
    }

    public NewsItem(NewsType type, String subject, String message, long createdAtMillis, boolean read) {
        this.type = type;
        this.subject = subject;
        this.message = message;
        this.createdAtMillis = createdAtMillis;
        this.read = read;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public NewsType getType() {
        return type;
    }

    public void setType(NewsType type) {
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
