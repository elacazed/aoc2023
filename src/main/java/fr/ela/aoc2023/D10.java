package fr.ela.aoc2023;


import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;
import java.util.function.BiFunction;
import java.util.function.Function;
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
            Position prev = loop.getLast();
            while (! next.equals(startPosition)) {
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
                next = switch (resolveStartType()) {
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
    }

    @Override
    public void run() {
        Grid testGrid = new Grid(list(getTestInputPath()));
        LinkedList<Position> testLoop = testGrid.walk();
        System.out.println("Test grid start position pipe : "+testGrid.resolveStartType()+", most distant point in loop : "+ (testLoop.size() / 2));


        Grid grid = new Grid(list(getInputPath()));
        LinkedList<Position> loop = grid.walk();
        System.out.println("Grid start position pipe : "+grid.resolveStartType()+", most distant point in loop : "+ (loop.size() / 2));
    }
}
