package model.definition.plant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class PlantUpgrade {

    private final int level;
    private final String type;
    private final double value;
    private final String specialTag;

    @JsonCreator
    public PlantUpgrade(
            @JsonProperty("level") int level,
            @JsonProperty("type") String type,
            @JsonProperty("value") double value,
            @JsonProperty("specialTag") String specialTag) {
        this.level = level;
        this.type = type;
        this.value = value;
        this.specialTag = specialTag == null ? "" : specialTag;
    }

    public int getLevel() {
        return level;
    }

    public String getType() {
        return type;
    }

    public double getValue() {
        return value;
    }

    public String getSpecialTag() {
        return specialTag;
    }

    public boolean hasSpecialTag() {
        return specialTag != null && !specialTag.isBlank();
    }

    @Override
    public String toString() {
        return "PlantUpgrade{level=" + level
                + ", type='" + type + '\''
                + ", value=" + value
                + (hasSpecialTag() ? ", specialTag='" + specialTag + '\'' : "")
                + '}';
    }
}
