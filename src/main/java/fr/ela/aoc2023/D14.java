package fr.ela.aoc2023;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

public class D14 extends AoC {

    record State(String platform) {
    }

    record Position(int x, int y) {
        Position north() {
            return new Position(x, y - 1);
        }

        Position south() {
            return new Position(x, y + 1);
        }

        Position west() {
            return new Position(x - 1, y);
        }

        Position east() {
            return new Position(x + 1, y);
        }
    }

    public enum Direction {
        NORTH(Position::north, Position::south, Comparator.comparing(Position::y)),
        WEST(Position::west, Position::east, Comparator.comparing(Position::x)),
        SOUTH(Position::south, Position::north, Comparator.comparing(Position::y).reversed()),
        EAST(Position::east, Position::west, Comparator.comparing(Position::x).reversed());

        private final UnaryOperator<Position> direction;
        private final UnaryOperator<Position> opposite;
        private final Comparator<Position> sort;

        Direction(UnaryOperator<Position> direction, UnaryOperator<Position> opposite, Comparator<Position> sort) {
            this.direction = direction;
            this.opposite = opposite;
            this.sort = sort;
        }
    }

    record Rock(char rock) {

    }

    public class Platform {

        final int width;
        final int height;
        final Map<Position, Rock> rocks = new HashMap<>();

        boolean available(Position position) {
            return !rocks.containsKey(position) && position.x >= 0 && position.y >= 0 && position.x < width && position.y < height;
        }


        public Platform(List<String> lines) {
            height = lines.size();
            width = lines.get(0).length();
            for (int y = 0; y < height; y++) {
                String line = lines.get(y);
                for (int x = 0; x < width; x++) {
                    char c = line.charAt(x);
                    if (c != '.') {
                        rocks.put(new Position(x, y), new Rock(line.charAt(x)));
                    }
                }
            }
        }

        Position slide(Position pos, UnaryOperator<Position> direction, UnaryOperator<Position> opposite) {
            Position next = direction.apply(pos);
            while (available(next)) {
                next = direction.apply(next);
            }
            return opposite.apply(next);
        }

        public void tilt(Direction direction) {
            List<Map.Entry<Position, Rock>> rollingRocks = rocks.entrySet().stream().filter(e -> e.getValue().rock == 'O')
                    .sorted(Map.Entry.comparingByKey(direction.sort)).toList();
            for (var e : rollingRocks) {
                Position pos = e.getKey();
                Position north = slide(pos, direction.direction, direction.opposite);
                Rock rock = rocks.remove(pos);
                rocks.put(north, rock);
            }
            //System.out.println("Tilting "+direction+"\n"+this);
        }

        public State step() {
            for (Direction direction : Direction.values()) {
                tilt(direction);
            }
            return new State(toString());
        }

        long getTotalLoad() {
            return rocks.entrySet().stream().filter(e -> e.getValue().rock == 'O')
                    .mapToLong(e -> (height - e.getKey().y))
                    .sum();
        }

        char get(int x, int y) {
            Rock dot = new Rock('.');
            return rocks.getOrDefault(new Position(x, y), dot).rock;
        }

        public String toString() {
            List<String> sb = new ArrayList<>();
            for (int y = 0; y < height; y++) {
                char[] line = new char[width];
                for (int x = 0; x < width; x++) {
                    line[x] = get(x, y);
                }
                sb.add(new String(line));
            }
            return String.join("\n", sb);
        }

        public long getLoad(int steps) {
            List<State> states = new ArrayList<>();
            states.add(step());
            int count = 1;
            while (count < steps && !states.contains(step())) {
                states.add(new State(toString()));
                count++;
            }
            int offset = states.indexOf(new State(toString()));
            int cycleLength = count - offset;
            int remaining = (steps - 1 - offset) % cycleLength;
            State end = states.get(offset + remaining);
            Platform platform = new Platform(Arrays.stream(end.platform.split("\n")).toList());
            return platform.getTotalLoad();
        }
    }

    @Override
    public void run() {
        Platform test = new Platform(list(getTestInputPath()));
        test.tilt(Direction.NORTH);
        System.out.println("Test Load part 1 (136) : " + test.getTotalLoad());
        long testPart2 = new Platform(list(getTestInputPath())).getLoad(1_000_000_000);
        System.out.println("Test Load part 2 (64) : " + testPart2);

        Platform platform = new Platform(list(getInputPath()));
        platform.tilt(Direction.NORTH);
        System.out.println("Load part 1 (105784) : " + platform.getTotalLoad());
        platform = new Platform(list(getInputPath()));
        long part2 = platform.getLoad(1_000_000_000);
        System.out.println("Load part 2 (91286) : " + part2);
    }
}

