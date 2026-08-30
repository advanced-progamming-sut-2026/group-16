package io.github.finalwave.network.sync;

public final class UnlockContentPayload {
    private String kind;
    private String name;

    public UnlockContentPayload() {
    }

    public UnlockContentPayload(String kind, String name) {
        this.kind = kind;
        this.name = name;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
