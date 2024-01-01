package fr.ela.aoc2023.utils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Path<K, V extends Comparable<V>> implements Comparable<Path<K, V>> {
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

    @Override
    public int compareTo(Path<K, V> o) {
        return this.cost.compareTo(o.cost);
    }

    public String toString() {
        return path.stream().map(Objects::toString).collect(Collectors.joining("->" ))+" ["+cost+"]";
    }
}
