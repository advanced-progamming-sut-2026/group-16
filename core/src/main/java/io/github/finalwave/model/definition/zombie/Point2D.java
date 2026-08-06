package io.github.finalwave.model.definition.zombie;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class Point2D {

    @JsonProperty("x")
    private final double x;

    @JsonProperty("y")
    private final double y;

    public Point2D(@JsonProperty("x") double x,
                   @JsonProperty("y") double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    @Override
    public String toString() {
        return "Point2D{x=" + x + ", y=" + y + "}";
    }
}
