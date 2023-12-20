package fr.ela.aoc2023.utils;

public record Position(int x, int y) {

    public int distance(Position other) {
        return other.y - y + other.x - x;
    }

    public int distance() {
        return x+y;
    }
}
