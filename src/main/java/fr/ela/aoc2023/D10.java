package fr.ela.aoc2023;


import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class D10 extends AoC {

    Map<Pattern, Character> possible = buildPossibles();

    private Map<Pattern, Character> buildPossibles() {
        Map possibles = new HashMap();
        possibles.put(Pattern.compile("[\\|7F][\\-7J].."), 'L');
        possibles.put(Pattern.compile("[\\|7F].[\\|LJ]."), '|');
        possibles.put(Pattern.compile("[\\|7F]..[\\-FL]."), 'J');
        possibles.put(Pattern.compile("..[\\|LJ][\\-LF]"), '7');
        possibles.put(Pattern.compile(".[\\-J7][\\|LJ]."), 'F');
        possibles.put(Pattern.compile(".[\\-LF].[\\-7J]."), '-');
        return possibles;
    }

    record Position(int x, int y) {
    }

    public class Grid {
        Map<Position, Character> grid;
        private final int width;
        private final int height;
        Position startPosition;

        public Grid(List<String> lines) {
            height = lines.size();
            width = lines.get(0).length();
            grid = new HashMap<>();
            String line;
            for (int y = 0; y < height; y++) {
                line = lines.get(y);
                for (int x = 0; x < width; x++) {
                    char c = line.charAt(x);
                    if (c != '.') {
                        Position pos = new Position(x, y);
                        if (c == 'S') {
                            startPosition = pos;
                        }
                        grid.put(pos, c);
                    }
                }
            }
            grid.put(startPosition, resolveStartType());
        }

        char charAt(Position pos) {
            return grid.getOrDefault(pos, '.');
        }

        private char resolveStartType() {
            char up = charAt(up(startPosition));
            char down = charAt(down(startPosition));
            char left = charAt(left(startPosition));
            char right = charAt(right(startPosition));
            String neighbors = new String(new char[]{up, right, down, left});
            return possible.entrySet().stream().filter(e -> e.getKey().matcher(neighbors).matches()).map(Map.Entry::getValue).findFirst().orElseThrow();
        }

        public LinkedList<Position> walk() {
            LinkedList<Position> loop = new LinkedList<>();
            loop.add(startPosition);
            Position next = next(startPosition, null);
            Position prev;
            while (!next.equals(startPosition)) {
                prev = loop.getLast();
                loop.add(next);
                next = next(loop.getLast(), prev);
            }
            return loop;
        }

        private Position valid(Position pos) {
            return (pos.x >= 0 && pos.y >= 0 && pos.x < width && pos.y < height) ? pos : null;
        }

        private Position left(Position pos) {
            return valid(new Position(pos.x - 1, pos.y));
        }

        private Position right(Position pos) {
            return valid(new Position(pos.x + 1, pos.y));
        }

        private Position up(Position pos) {
            return valid(new Position(pos.x, pos.y - 1));
        }

        private Position down(Position pos) {
            return valid(new Position(pos.x, pos.y + 1));
        }

        Position next(Position current, Position from) {
            Position up = up(current);
            Position down = down(current);
            Position left = left(current);
            Position right = right(current);
            Position next = null;
            if (current.equals(startPosition)) {
                next = switch (charAt(startPosition)) {
                    case '|', 'L', 'J' -> up;
                    case '-', 'F' -> right;
                    case '7' -> down;
                    default -> throw new IllegalArgumentException();
                };
            } else {
                next = switch (charAt(current)) {
                    case '|' -> from.equals(up) ? down : up;
                    case '-' -> from.equals(left) ? right : left;
                    case 'L' -> from.equals(up) ? right : up;
                    case 'J' -> from.equals(left) ? up : left;
                    case '7' -> from.equals(left) ? down : left;
                    case 'F' -> from.equals(right) ? down : right;
                    default -> throw new IllegalArgumentException("Illegql char " + charAt(current) + " at " + current);
                };
            }
            return next;
        }

        boolean isAnAngle(Position p) {
            char c = charAt(p);
            return switch (c) {
                case 'J', 'L', '7', 'F' -> true;
                default -> false;
            };
        }

        /**
         * Pick's theorem (https://en.wikipedia.org/wiki/Pick%27s_theorem)
         * loopArea = interiorPointsCount + (boundaryPointsCount / 2) - 1
         * <p>
         * Part 2 answer is interiorPointsCount
         * transforming Pick's formula:
         * interiorPointsCount = loopArea - (boundaryPointsCount / 2) + 1
         * <p>
         * boundaryPointsCount is length of loop (practically part1 answer * 2)
         * <p>
         * loopArea can by calculated using Shoelace formula (https://en.wikipedia.org/wiki/Shoelace_formula):
         * angles = (x1, y1) (x2, y2) (x3, y3) ...
         * 2 * loopArea = x1 * y2 - y1 * x2 + x2 * y3 - x3 * y2 + ...
         * loopArea = result / 2
         */
        public long getLoopArea(LinkedList<Position> loop) {
            int loopLength = loop.size();
            //vertices are delimited by direction changes, eg : F,J,7,L
            List<Position> polygon = loop.stream().filter(this::isAnAngle).toList();
            int polygonSize = polygon.size();
            var area = 0;

            for (int i = 0; i < polygonSize; i++) {
                int nextIndex = (i + 1) % polygonSize;
                Position current = polygon.get(i);
                Position next = polygon.get(nextIndex);
                area += current.x * next.y - current.y * next.x;
            }

            area = Math.abs(area) / 2;
            return area - loopLength / 2 + 1;
        }


        boolean isUpConnection(Position p) {
            char c = charAt(p);
            return switch (c) {
                case 'J', 'L', '|' -> true;
                default -> false;
            };
        }

        public long pointsInsideLoop(LinkedList<Position> loop) {
            // remove all pipes int grid that are not part of the loop
            Set<Position> pos = new HashSet<>(grid.keySet());
            loop.forEach(pos::remove);
            pos.forEach(grid::remove);
            int points = 0;
            for (int y = 0; y < height; y++) {
                int vertPipeCount = 0;
                for (int x = 0; x < width; x++) {
                    Position p = new Position(x, y);
                    if (isUpConnection(p)) {
                        vertPipeCount++;
                    }
                    if (charAt(p) == '.' && vertPipeCount % 2 == 1) {
                        points++;
                    }
                }
            }
            return points;
        }

    }


    @Override
    public void run() {
        Grid testGrid = new Grid(list(getTestInputPath()));
        LinkedList<Position> testLoop = testGrid.walk();
        System.out.println("Test grid start position pipe : " + testGrid.charAt(testGrid.startPosition) + ", most distant point in loop : " + (testLoop.size() / 2));
        Grid testGrid2 = new Grid(list(getPath("input-test2")));
        LinkedList<Position> testLoop2 = testGrid2.walk();
        System.out.println("Test loop points count (4) : first solution : " + testGrid2.pointsInsideLoop(testLoop2) + " Second solution : " + testGrid2.getLoopArea(testLoop2));


        Grid grid = new Grid(list(getInputPath()));
        LinkedList<Position> loop = grid.walk();
        System.out.println("Grid start position pipe : " + grid.charAt(grid.startPosition) + ", most distant point in loop (6886): " + (loop.size() / 2));
        System.out.println("Loop points count (371): first solution : " + grid.pointsInsideLoop(loop) + " Second solution : " + grid.getLoopArea(loop));
    }
}
