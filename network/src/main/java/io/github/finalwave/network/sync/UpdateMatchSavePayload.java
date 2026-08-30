package io.github.finalwave.network.sync;

import com.fasterxml.jackson.databind.JsonNode;

public final class UpdateMatchSavePayload {
    private JsonNode snapshot;

    public UpdateMatchSavePayload() {
    }

    public JsonNode getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(JsonNode snapshot) {
        this.snapshot = snapshot;
    }
}
