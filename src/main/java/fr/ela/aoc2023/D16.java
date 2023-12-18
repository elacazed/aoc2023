package fr.ela.aoc2023;

import java.util.Deque;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public class D16 extends AoC {

    record State(Position position, Direction direction) {
    }

    enum Direction {
        NORTH(p -> new Position(p.x, p.y - 1)),
        EAST(p -> new Position(p.x + 1, p.y)),
        SOUTH(p -> new Position(p.x, p.y + 1)),
        WEST(p -> new Position(p.x - 1, p.y));

        final Function<Position, Position> move;

        Direction(Function<Position, Position> move) {
            this.move = move;
        }
    }

    record Position(int x, int y) {
        Position move(Direction to) {
            Position p = to.move.apply(this);
            return p;
        }
    }

    record Mirror(char c) {
        Set<Direction> to(Direction current) {
            return switch (c) {
                case '|' -> switch (current) {
                    case NORTH, SOUTH -> EnumSet.of(current);
                    case EAST, WEST -> EnumSet.of(Direction.NORTH, Direction.SOUTH);
                };
                case '-' -> switch (current) {
                    case NORTH, SOUTH -> EnumSet.of(Direction.WEST, Direction.EAST);
                    case EAST, WEST -> EnumSet.of(current);
                };
                case '/' -> switch (current) {
                    case NORTH -> EnumSet.of(Direction.EAST);
                    case SOUTH -> EnumSet.of(Direction.WEST);
                    case EAST -> EnumSet.of(Direction.NORTH);
                    case WEST -> EnumSet.of(Direction.SOUTH);
                };
                case '\\' -> switch (current) {
                    case NORTH -> EnumSet.of(Direction.WEST);
                    case SOUTH -> EnumSet.of(Direction.EAST);
                    case EAST -> EnumSet.of(Direction.SOUTH);
                    case WEST -> EnumSet.of(Direction.NORTH);
                };
                default -> EnumSet.of(current);
            };
        }
    }

    class Grid {
        final int width, height;
        Map<Position, Mirror> mirrors = new HashMap<>();

        Grid(List<String> lines) {
            height = lines.size();
            width = lines.get(0).length();
            for (int y = 0; y < height; y++) {
                String line = lines.get(y);
                for (int x = 0; x < width; x++) {
                    char c = line.charAt(x);
                    if (c != '.') {
                        mirrors.put(new Position(x, y), new Mirror(line.charAt(x)));
                    }
                }
            }
        }

        boolean in(Position pos) {
            return pos.x >= 0 && pos.x < width && pos.y >= 0 && pos.y < height;
        }

        List<State> next(State current) {
            return Optional.ofNullable(mirrors.get(current.position)).map(m -> m.to(current.direction)).orElse(EnumSet.of(current.direction))
                    .stream()
                    .map(d -> new State(current.position.move(d), d))
                    .filter(s -> in(s.position))
                    .toList();
        }

        long energize() {
            Set<State> states = energize(new State(new Position(0, 0), Direction.EAST), new HashSet<>());
            return states.stream().map(s -> s.position).distinct().count();
        }

        Set<State> energize(State initial, Set<State> visited) {

            Deque<State> stack = new LinkedList<>();
            while (!visited.contains(initial)) {
                List<State> nextStates = next(initial);
                visited.add(initial);
                if (nextStates.isEmpty()) {
                    break;
                }
                initial = nextStates.get(0);
                if (nextStates.size() == 2) {
                    State other = nextStates.get(1);
                    if (!visited.contains(other)) {
                        stack.push(other);
                    }
                }
            }
            while (!stack.isEmpty()) {
                energize(stack.pop(), visited);
            }
            return visited;
        }

    }

    @Override
    public void run() {
        Grid testGrid = new Grid(list(getTestInputPath()));
        long testEnergized = testGrid.energize();
        System.out.println("Test Grid energized Tiles count (46) : " + testEnergized);
        Grid grid = new Grid(list(getInputPath()));
        long energized = grid.energize();
        System.out.println("Energized Tiles count (7199) : " + energized);
    }
}

