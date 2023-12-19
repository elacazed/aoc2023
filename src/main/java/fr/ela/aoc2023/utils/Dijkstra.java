package fr.ela.aoc2023.utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;

public class Dijkstra<N, C extends Comparable<C>> {
    private final Function<N, List<N>> advance;
    private final Function<N, C> costFunction;

    private final BinaryOperator<C> costAccumulator;

    public Dijkstra(Function<N, List<N>> advance, Function<N, C> costFunction, BinaryOperator<C> accumulator) {
        this.advance = advance;
        this.costFunction = costFunction;
        this.costAccumulator = accumulator;
    }

    public static <K> Dijkstra<K, Long> longDijkstra(Function<K, List<K>> advance, Function<K, Long> costFunction) {
        return new Dijkstra<>(advance, costFunction, Long::sum);
    }

    public Path<N, C> findShortestPath(List<N> start, Predicate<N> endReached) {
        HashSet<N> visited = new HashSet<>();

        PriorityQueue<State> queue = new PriorityQueue<>(Comparator.comparing(s -> s.cost));

        start.stream().map(s -> new State(s, costFunction.apply(s), null)).forEach(queue::add);
        while (!queue.isEmpty()) {
            State cur = queue.remove();
            if (visited.contains(cur.node)) {
                continue;
            }
            visited.add(cur.node);
            if (endReached.test(cur.node)) {
                C cost = cur.cost;
                LinkedList<N> path = new LinkedList<>();
                path.addFirst(cur.node);
                while (cur.prev != null) {
                    path.addFirst(cur.prev.node);
                    cur = cur.prev;
                }
                return new Path<>(path, cost);
            }
            List<N> next = advance.apply(cur.node);
            for (N n : next) {
                State s = new State(n, costAccumulator.apply(cur.cost, costFunction.apply(n)), cur);
                queue.add(s);
            }
        }
        return new Path<>(List.of(), null);
    }

    class State {
        public final N node;
        public final C cost;
        public final State prev;

        public State(N node, C cost, State prev) {
            this.node = node;
            this.cost = cost;
            this.prev = prev;
        }

        @Override
        public String toString() {
            return node.toString()+" : "+cost;
        }
    }
}
