package fr.ela.aoc2023.utils;

public enum Direction {

    NORTH(0, -1),
    EAST(1, 0),
    SOUTH(0, 1),
    WEST(-1, 0);

    final int xOffset;
    final int yOffset;

    Direction(int xOffset, int yOffset) {
        this.xOffset = xOffset;
        this.yOffset = yOffset;
    }
    public Position move(Position from) {
        return move(from, 1);
    }

    public Position move(Position from, int amount) {
        return new Position(from.x() + amount * xOffset, from.y() + amount * yOffset);
    }
    public Direction left() {
        return switch (this) {
            case NORTH -> WEST;
            case WEST -> SOUTH;
            case SOUTH -> EAST;
            case EAST -> NORTH;
        };
    }

    public Direction right() {
        return switch (this) {
            case NORTH -> EAST;
            case EAST -> SOUTH;
            case SOUTH -> WEST;
            case WEST -> NORTH;
        };
    }

    public Direction opposite() {
        return switch (this) {
            case NORTH -> SOUTH;
            case WEST -> EAST;
            case SOUTH -> NORTH;
            case EAST -> WEST;
        };
    }
}
