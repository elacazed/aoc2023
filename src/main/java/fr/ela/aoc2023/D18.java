package fr.ela.aoc2023;

import fr.ela.aoc2023.utils.Direction;
import fr.ela.aoc2023.utils.Position;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class D18 extends AoC {

    private static final Pattern hexaPattern = Pattern.compile(".*\\(#([a-f0-9]{6})\\)");

    public record Move(Direction direction, int length, String value) {
        Position end(Position start) {
            return direction.move(start, length);
        }

        Move hexa() {
            Matcher matcher = hexaPattern.matcher(value);
            if (matcher.matches()) {
                String hexa = matcher.group(1);
                Direction direction = switch (hexa.charAt(5)) {
                    case '0' -> Direction.EAST;
                    case '2' -> Direction.WEST;
                    case '1' -> Direction.SOUTH;
                    case '3' -> Direction.NORTH;
                    default -> throw new IllegalArgumentException();
                };
                int length = Integer.parseInt(hexa.substring(0, 5), 16);
                return new Move(direction, length, value);
            }
            throw new IllegalArgumentException(value);
        }

        static Move parse(String line) {
            String[] args = line.split(" ");
            Direction direction = switch (args[0].charAt(0)) {
                case 'R' -> Direction.EAST;
                case 'L' -> Direction.WEST;
                case 'D' -> Direction.SOUTH;
                case 'U' -> Direction.NORTH;
                default -> throw new IllegalArgumentException();
            };
            return new Move(direction, Integer.parseInt(args[1]), line);
        }
    }

    /**
     * From day 10
     *
     * Given :
     *   A = internal area (without points from the edge)
     *   b = boundary integer points (ie : sum of all segments length)
     *   i = number of integer points inside the polygon
     *
     * We want i + b
     *
     * sholeace formula => A.
     *
     * Pick's theorem :
     *    A = i + b/2 - 1 => i = A - b/2 + 1
     *
     * => i+b = A + b/2 + 1
     */
    public long getLoopArea(List<Move> loop) {
        Position start = new Position(0,0);
        long area = 0;
        for (Move move : loop) {
            Position next = move.end(start);
            area += (long) start.x() * next.y() - (long) start.y() * next.x();
            start = next;
        }
        area = Math.abs(area) / 2;
        long b = loop.stream().mapToLong(l -> l.length).sum();
        return area + b / 2 + 1;
    }


    @Override
    public void run() {
        List<Move> testMoves = list(getTestInputPath(), Move::parse);
        long testArea = getLoopArea(testMoves);
        System.out.println("Test Lavaduct Lagoon volume (62) : "+testArea);
        testArea = getLoopArea(testMoves.stream().map(Move::hexa).toList());
        System.out.println("Test Lavaduct Hexa Lagoon volume (952408144115) : "+testArea);

        List<Move> moves = list(getInputPath(), Move::parse);
        long area = getLoopArea(moves);
        System.out.println("Lavaduct Lagoon volume (40131) : "+area);
        area = getLoopArea(moves.stream().map(Move::hexa).toList());
        System.out.println("Lavaduct Hexa Lagoon volume (104454050898331) : "+area);

    }
}

