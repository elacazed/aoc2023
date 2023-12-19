package fr.ela.aoc2023.utils;

import java.util.List;
import java.util.stream.Stream;

public class Path<K, V> {
    private final List<K> path;
    private final V cost;

    public Path(List<K> path, V cost) {
        this.path = path;
        this.cost = cost;
    }

    public V cost() {
        return cost;
    }

    public Stream<K> stream() {
        return path.stream();
    }

    public List<K> path() {
        return path;
    }
}
