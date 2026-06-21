package model.definition.zombie;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class Rect {

    @JsonProperty("mHeight")
    private final int height;

    @JsonProperty("mWidth")
    private final int width;

    @JsonProperty("mX")
    private final int x;

    @JsonProperty("mY")
    private final int y;

    public Rect(@JsonProperty("mHeight") int height,
                @JsonProperty("mWidth") int width,
                @JsonProperty("mX") int x,
                @JsonProperty("mY") int y) {
        this.height = height;
        this.width = width;
        this.x = x;
        this.y = y;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public String toString() {
        return "Rect{x=" + x + ", y=" + y + ", w=" + width + ", h=" + height + "}";
    }
}
