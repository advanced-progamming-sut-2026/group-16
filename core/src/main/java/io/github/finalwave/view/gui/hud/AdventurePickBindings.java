package io.github.finalwave.view.gui.hud;

import io.github.finalwave.model.collection.CollectionPlantDetail;
import io.github.finalwave.model.collection.CollectionPlantEntry;

import java.util.List;

public interface AdventurePickBindings {

    default CollectionPlantEntry plantEntry(String name) {
        return null;
    }

    default CollectionPlantDetail plantDetail(String name) {
        return null;
    }

    default int plantCost(String name) {
        return 0;
    }

    default boolean plantSelectable(String name) {
        return true;
    }

    default boolean plantBoosted(String name) {
        return false;
    }

    default List<String> previewLaneNames() {
        return List.of();
    }
}
