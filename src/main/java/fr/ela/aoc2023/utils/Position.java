package fr.ela.aoc2023.utils;

import java.util.Arrays;
import java.util.List;

public record Position(int x, int y) {

    public int distance(Position other) {
        return other.y - y + other.x - x;
    }

    public int distance() {
        return x+y;
    }

    public List<Position> cardinals() {
        return Arrays.stream(Direction.values()).map(d -> d.move(this)).toList();
    }
}
