package io.github.finalwave.model.collection;

public record CollectionPlantQuery(CollectionPlantFilter filter, String family) {
    public static CollectionPlantQuery all() {
        return new CollectionPlantQuery(CollectionPlantFilter.ALL, null);
    }

    public CollectionPlantQuery withFilter(CollectionPlantFilter next) {
        return new CollectionPlantQuery(next == null ? CollectionPlantFilter.ALL : next, family);
    }

    public CollectionPlantQuery withFamily(String nextFamily) {
        return new CollectionPlantQuery(filter == null ? CollectionPlantFilter.ALL : filter, nextFamily);
    }
}
