package fr.ela.aoc2023.utils;

public enum Direction {

    NORTH(0, -1),
    EAST(1, 0),
    SOUTH(0, 1),
    WEST(-1, 0);

    final int xOffset;
    final int yOffset;

    Direction(int xOfsset, int yOffset) {
        this.xOffset = xOfsset;
        this.yOffset = yOffset;
    }

    public Position move(Position from) {
        return new Position(from.x() + xOffset, from.y() + yOffset);
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
}