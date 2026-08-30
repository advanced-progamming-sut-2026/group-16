package io.github.finalwave.network;

import com.fasterxml.jackson.databind.JsonNode;

public final class MessageEnvelope {
    private String type;
    private String requestId;
    private JsonNode payload;

    public MessageEnvelope() {
    }

    public MessageEnvelope(String type, String requestId, JsonNode payload) {
        this.type = type;
        this.requestId = requestId;
        this.payload = payload;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public JsonNode getPayload() {
        return payload;
    }

    public void setPayload(JsonNode payload) {
        this.payload = payload;
    }
}
