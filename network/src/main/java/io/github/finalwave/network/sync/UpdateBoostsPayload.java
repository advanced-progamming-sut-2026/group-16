package io.github.finalwave.network.sync;

import java.util.ArrayList;
import java.util.List;

public final class UpdateBoostsPayload {
    private List<String> plantTypes = new ArrayList<>();

    public UpdateBoostsPayload() {
    }

    public List<String> getPlantTypes() {
        return plantTypes;
    }

    public void setPlantTypes(List<String> plantTypes) {
        this.plantTypes = plantTypes == null ? new ArrayList<>() : plantTypes;
    }
}
