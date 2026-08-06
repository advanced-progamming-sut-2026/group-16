package io.github.finalwave.model.definition.zombie;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class ZombieStat {

    @JsonProperty("Type")
    private final String type;

    @JsonProperty("Value")
    private final String value;

    public ZombieStat(@JsonProperty("Type") String type,
                      @JsonProperty("Value") String value) {
        this.type = type;
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "ZombieStat{type=" + type + ", value=" + value + "}";
    }
}
