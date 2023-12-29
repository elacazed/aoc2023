package fr.ela.aoc2023;

import fr.ela.aoc2023.utils.Direction;
import fr.ela.aoc2023.utils.Grid;
import fr.ela.aoc2023.utils.Path;
import fr.ela.aoc2023.utils.Position;
import fr.ela.aoc2023.utils.Walker;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class D23 extends AoC {

    public enum Track {
        PATH(null, '.'),
        EAST_SLOPE(Direction.EAST, '>'),
        WEST_SLOPE(Direction.WEST, '<'),
        NORTH_SLOPE(Direction.NORTH, '^'),
        SOUTH_SLOPE(Direction.SOUTH, 'v');

        private final Direction forced;
        private final Set<Direction> directions;
        private final char c;

        Track(Direction forced, char c) {
            this.directions = forced == null ? EnumSet.allOf(Direction.class) : EnumSet.of(forced);
            this.forced = forced;
            this.c = c;
        }

        public static Track of(char c) {
            return switch (c) {
                case '.' -> PATH;
                case '>' -> EAST_SLOPE;
                case '<' -> WEST_SLOPE;
                case '^' -> NORTH_SLOPE;
                case 'v' -> SOUTH_SLOPE;
                case '#' -> null;
                default -> throw new IllegalArgumentException();
            };
        }

        public boolean isOpposite(Direction d) {
            return d.opposite() == forced;
        }

        public Set<Direction> directions() {
            return directions;
        }
    }

    public record Hike(Position position, long cost) {

        public int y() {
            return position.y();
        }

        public int x() {
            return position.x();
        }

    }

    public static class Mountain {
        private final Grid<Track> grid;

        public Mountain(Grid<Track> grid) {
            this.grid = grid;
        }

        public Mountain(List<String> lines) {
            grid = Grid.parseCharactersGrid(lines, Track::of);
        }

        List<Hike> next(Hike position) {
            Track t = grid.get(position.position);
            return t.directions.stream().map(d -> move(position.position, d))
                    .filter(Objects::nonNull)
                    .map(p -> new Hike(p, 1)).toList();
        }

        private Position move(Position from, Direction d) {
            Position next = d.move(from);
            return (grid.contains(next) && !grid.get(next).isOpposite(d)) ? next : null;
        }


        public long getLongestHike() {
            final Hike start = new Hike(new Position(1, 0), 0);
            final Position end = new Position(grid.getWidth() - 2, grid.getHeight() - 1);
            Walker<Hike, Long> walker = Walker.longWalker(this::next, p -> p.cost);
            List<Path<Hike, Long>> paths = walker.findAllPaths(start, s -> s.position.equals(end));
            //for (var trail : paths) {
            //    List<char[]> chars = grid.draw(t -> t == null ? ' ' : t.getChar());
            //    trail.path().forEach(n -> {
            //        chars.get((int) n.y())[(int) n.x()] = 'O';
            //    });
            //    System.out.println("-------------- Path : ");
            //    System.out.println(chars.stream().map(String::new).collect(Collectors.joining("\n")));
            //}
            return paths.stream().mapToLong(Path::cost).max().orElseThrow();
        }

    }

    @Override
    public void run() {
        Mountain testMountain = new Mountain(list(getTestInputPath()));
        System.out.println("Longest test hike (94) : " + testMountain.getLongestHike());

        Mountain mountain = new Mountain(list(getInputPath()));
        System.out.println("Longest hike (2162) : " + mountain.getLongestHike());
    }

}

