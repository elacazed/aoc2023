package fr.ela.aoc2023.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;

public class Walker<N, C extends Comparable<C>> {
    private final Function<N, List<N>> advance;
    private final Function<N, C> costFunction;
    private final BinaryOperator<C> costAccumulator;
    private final Comparator<State<N, C>> comparator;

    public Walker(Function<N, List<N>> advance, Function<N, C> costFunction, Comparator<N> tieBreaker, BinaryOperator<C> accumulator) {
        this.advance = advance;
        this.costFunction = costFunction;
        this.costAccumulator = accumulator;
        Comparator<State<N, C>> costComparator = Comparator.comparing(s -> s.cost);
        if (tieBreaker != null) {
            Comparator<State<N, C>> comp = (s1, s2) -> tieBreaker.compare(s1.node, s2.node);
            this.comparator = costComparator.thenComparing(comp);
        } else {
            this.comparator = costComparator;
        }
    }

    public Walker(Function<N, List<N>> advance, Function<N, C> costFunction, BinaryOperator<C> accumulator) {
        this(advance, costFunction, null, accumulator);
    }

    public static <K> Walker<K, Long> longWalker(Function<K, List<K>> advance, Function<K, Long> costFunction) {
        return new Walker<>(advance, costFunction, Long::sum);
    }

    public static <K> Walker<K, Long> longWalker(Function<K, List<K>> advance, Function<K, Long> costFunction, Comparator<K> tieBreaker) {
        return new Walker<>(advance, costFunction, tieBreaker, Long::sum);
    }

    public static <K> Walker<K, Integer> intWalker(Function<K, List<K>> advance, Function<K, Integer> costFunction) {
        return new Walker<>(advance, costFunction, Integer::sum);
    }


    public Path<N, C> findShortestPath(List<N> start, Predicate<N> endReached) {
        HashSet<N> visited = new HashSet<>();
        PriorityQueue<State<N, C>> queue = new PriorityQueue<>(comparator);

        start.stream().map(s -> new State<>(s, costFunction.apply(s), null)).forEach(queue::add);
        while (!queue.isEmpty()) {
            State<N, C> cur = queue.remove();
            if (visited.contains(cur.node)) {
                continue;
            }
            visited.add(cur.node);
            if (endReached.test(cur.node)) {
                return buildPath(cur);
            }
            List<N> next = advance.apply(cur.node);
            for (N n : next) {
                State<N, C> s = new State<>(n, costAccumulator.apply(cur.cost, costFunction.apply(n)), cur);
                queue.add(s);
            }
        }
        return new Path<>(List.of(), null);
    }

    public Collection<Path<N, C>> findAllPaths(N start, Predicate<N> endReached) {
        return findAllPaths(start, endReached, Object::equals);
    }

    public Collection<Path<N, C>> findAllPaths(N start, Predicate<N> endReached, BiPredicate<N, N> alreadySteppedHere) {
        var queue = new LinkedList<State<N, C>>();
        queue.add(new State<>(start, costFunction.apply(start), null));
        List<Path<N, C>> paths = new ArrayList<>();

        BiPredicate<State<N, C>, N> alreadySteppedFilter = (state, node) -> state.anyPrevStateMatches(alreadySteppedHere, node);
        while (!queue.isEmpty()) {
            var cur = queue.removeFirst();
            if (endReached.test(cur.node)) {
                paths.add(buildPath(cur));
            } else {
                advance.apply(cur.node).stream()
                        .filter(n -> !alreadySteppedFilter.test(cur, n))
                        .map(n -> new State<>(n, costAccumulator.apply(cur.cost, costFunction.apply(n)), cur))
                        .forEach(queue::add);
            }
        }
        return paths;
    }

    private static <K, V extends Comparable<V>> Path<K, V> buildPath(State<K, V> cur) {
        V cost = cur.cost();
        LinkedList<K> path = new LinkedList<>();
        path.addFirst(cur.node());
        while (cur.prev() != null) {
            path.addFirst(cur.prev().node());
            cur = cur.prev();
        }
        return new Path<>(path, cost);
    }

    record State<N, C>(N node, C cost, State<N, C> prev) {

        @Override
        public String toString() {
            return node.toString() + " : " + cost;
        }

        public boolean anyPrevStateMatches(BiPredicate<N, N> predicate, N node) {
            State<N, C> state = this;
            while (state != null) {
                if (predicate.test(state.node, node)) {
                    return true;
                }
                state = state.prev;
            }
            return false;
        }

    }

}
