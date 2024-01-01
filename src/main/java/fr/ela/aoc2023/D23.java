package fr.ela.aoc2023;

import fr.ela.aoc2023.utils.Direction;
import fr.ela.aoc2023.utils.Grid;
import fr.ela.aoc2023.utils.Path;
import fr.ela.aoc2023.utils.Position;
import fr.ela.aoc2023.utils.Walker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiPredicate;

public class D23 extends AoC {

    public enum Track {
        PATH(null),
        EAST_SLOPE(Direction.EAST),
        WEST_SLOPE(Direction.WEST),
        NORTH_SLOPE(Direction.NORTH),
        SOUTH_SLOPE(Direction.SOUTH);

        private final Direction forced;
        private final Set<Direction> directions;

        Track(Direction forced) {
            this.directions = forced == null ? EnumSet.allOf(Direction.class) : EnumSet.of(forced);
            this.forced = forced;
        }

        public boolean isSlide() {
            return this != PATH;
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

    }


    public record Trail(Position start, Position end, int length) {
        public boolean isSame(Trail other) {
            return other.start.equals(start) || other.end.equals(end);
        }

        public String toString() {
            return start.toString();
        }
    }

    public static class Mountain {
        private final Grid<Track> grid;
        final Position start;
        final Position end;

        final Map<Position, List<Trail>> trails;

        public Mountain(Grid<Track> grid) {
            this.grid = grid;
            start = new Position(1, 0);
            end = new Position(grid.getWidth() - 2, grid.getHeight() - 1);
            this.trails = findTrails();
        }

        public Mountain(List<String> lines) {
            grid = Grid.parseCharactersGrid(lines, Track::of);
            start = new Position(1, 0);
            end = new Position(grid.getWidth() - 2, grid.getHeight() - 1);
            this.trails = findTrails();
        }

        public Mountain dry() {
            Grid<Track> drygrid = new Grid<>(grid.getWidth(), grid.getHeight());
            Arrays.stream(Track.values()).flatMap(t -> grid.getPositionsOf(t).stream())
                    .forEach(p -> drygrid.put(p, Track.PATH));
            return new Mountain(drygrid);
        }

        List<Position> next(Position position, Position prev) {
            Track t = grid.get(position);
            List<Position> res = t.directions.stream().map(d -> move(position, d)).toList();
            return res.stream()
                    .filter(Objects::nonNull)
                    .filter(p -> !p.equals(prev)).toList();
        }

        private Position move(Position from, Direction d) {
            Position next = d.move(from);
            return (grid.contains(next) && !grid.get(next).isOpposite(d)) ? next : null;
        }

        Map<Position, List<Trail>> findTrails() {
            Set<Position> crossings = new HashSet<>();
            crossings.add(start);
            crossings.add(end);
            Map<Position, List<Trail>> allTrails = new HashMap<>();

            for (Position pos : grid.getPositionsOf(Track.PATH)) {
                List<Position> cards = grid.to(pos, Set.of());
                if (cards.size() > 2) {
                    crossings.add(pos);
                }
            }
            for (Position crossing : crossings) {
                List<Position> outP = next(crossing, null);
                for (Position out : outP) {
                    Trail t = findNextNode(crossing, out);
                    if (t != null) {
                        allTrails.computeIfAbsent(t.start, s -> new ArrayList<>()).add(t);
                    }
                }
            }
            return allTrails;
        }

        boolean isStartOrEnd(Position p) {
            return p.equals(start) || p.equals(end);
        }

        private Trail findNextNode(Position start, Position direction) {
            int length = 1;
            List<Position> nextPositions = next(direction, start);
            Position prev = direction;
            while (nextPositions.size() < 2) { //
                length++;
                if (nextPositions.isEmpty()) {
                    if (isStartOrEnd(direction)) {
                        return new Trail(start, direction, length);
                    } else {
                        // Dead end.
                        return null;
                    }
                }
                direction = nextPositions.get(0);
                nextPositions = next(direction, prev);
                prev = direction;
            }
            return new Trail(start, direction, length);
        }

        List<Trail> nextTrails(Trail trail) {
            return trails.getOrDefault(trail.end, List.of());
        }

        public long getLongestHike(boolean dry) {
            Walker<Trail, Integer> walker = Walker.intWalker(this::nextTrails, Trail::length);
            Trail startingTrail = trails.get(start).stream().reduce((x, y) -> {
                throw new IllegalStateException();
            }).orElseThrow();

            BiPredicate<Trail, Trail> alreadyWentThere = dry ? Trail::isSame : Object::equals;

            return walker.findAllPaths(startingTrail, t -> t.end().equals(end), alreadyWentThere).stream()
                    .mapToLong(Path::cost).max().orElseThrow() - 1;
        }

    }

    @Override
    public void run() {
        Mountain testMountain = new Mountain(list(getTestInputPath()));
        long time = System.nanoTime();
        System.out.println("Longest test hike (94) : " + testMountain.getLongestHike(true) + " (" + this.formatDuration(time) + ")");
        Mountain testDryMountain = testMountain.dry();
        time = System.nanoTime();
        System.out.println("Longest test hike on dry mountain (154) : " + testDryMountain.getLongestHike(true) + " (" + this.formatDuration(time) + ")");

        Mountain mountain = new Mountain(list(getInputPath()));
        time = System.nanoTime();
        System.out.println("Longest hike (2162) : " + mountain.getLongestHike(false) + " (" + this.formatDuration(time) + ")");
        Mountain dryMountain = mountain.dry();
        time = System.nanoTime();
        System.out.println("Longest hike on dry mountain (6334) : " + dryMountain.getLongestHike(true) + " (" + this.formatDuration(System.nanoTime() - time) + ")");
    }

}

