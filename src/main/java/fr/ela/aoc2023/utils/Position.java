package fr.ela.aoc2023.utils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public record Position(int x, int y) {

    public int distance(Position other) {
        return Math.abs(other.y - y) + Math.abs(other.x - x);
    }

    public int distance() {
        return x+y;
    }

    public List<Position> cardinals() {
        return cards().toList();
    }

    public Stream<Position> cards() {
        return Arrays.stream(Direction.values()).map(d -> d.move(this));
    }

    public Position modulo(int xmax, int ymax) {
        return new Position(x % xmax, y % ymax);
    }

    public String toString() {
        return "["+x+","+y+"]";
    }
}
