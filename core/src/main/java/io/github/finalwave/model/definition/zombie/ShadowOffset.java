package io.github.finalwave.model.definition.zombie;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class ShadowOffset {

    @JsonProperty("x")
    private final double x;

    @JsonProperty("y")
    private final double y;

    @JsonProperty("z")
    private final double z;

    public ShadowOffset(@JsonProperty("x") double x,
                        @JsonProperty("y") double y,
                        @JsonProperty("z") double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    @Override
    public String toString() {
        return "ShadowOffset{x=" + x + ", y=" + y + ", z=" + z + "}";
    }
}
