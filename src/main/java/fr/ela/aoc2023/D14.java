package fr.ela.aoc2023;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class D14 extends AoC {

    record Position(int x, int y) {
        Position north() {
            return new Position(x, y - 1);
        }
    }

    record Rock(char rock) {

    }

    public class Platform {

        final int width;
        final int height;
        final Map<Position, Rock> rocks = new HashMap<>();

        Position north(Position pos) {
            if (pos.y == 0) {
                return pos;
            }
            Position north = new Position(pos.x, pos.y - 1);
            if (!rocks.containsKey(north)) {
                Position nnorth = new Position(north.x, north.y - 1);
                while (nnorth.y >= 0 && !rocks.containsKey(nnorth)) {
                    north = nnorth;
                    nnorth = new Position(north.x, north.y - 1);
                }
                return north;
            } else {
                return pos;
            }
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

        public void tiltNorth() {
            List<Map.Entry<Position, Rock>> rollingRocks = rocks.entrySet().stream().filter(e -> e.getValue().rock == 'O')
                    .sorted(Comparator.comparing(e -> e.getKey().y)).toList();
            for (var e : rollingRocks) {
                Position pos = e.getKey();
                Position north = north(pos);
                if (!north.equals(pos)) {
                    Rock rock = rocks.remove(pos);

                    rocks.put(north, rock);
                }
            }
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
    }

    @Override
    public void run() {
        Platform test = new Platform(list(getTestInputPath()));
        test.tiltNorth();
        System.out.println("Test Load part 1 (136) : " + test.getTotalLoad());

        Platform platform = new Platform(list(getInputPath()));
        platform.tiltNorth();
        System.out.println("Load part 1 (105784) : " + platform.getTotalLoad());
    }


}
