package fr.ela.aoc2023;

import fr.ela.aoc2023.utils.Position3D;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class D22 extends AoC {

    private static Pattern BRICK_PATTERN = Pattern.compile("([0-9]+),([0-9]+),([0-9]+)~([0-9]+),([0-9]+),([0-9]+)");

    public record Brick(int id, List<Position3D> positions) {

        String name() {
            return id < 26 ? "" + (char) ('A' + id) : Integer.toString(id);
        }

        private static Brick between(int id, Position3D start, Position3D end, ToIntFunction<Position3D> getter, IntFunction<Position3D> builder) {
            return new Brick(id, IntStream.range(Math.min(getter.applyAsInt(start), getter.applyAsInt(end)), Math.max(getter.applyAsInt(start), getter.applyAsInt(end)) + 1)
                    .mapToObj(builder)
                    .toList());
        }

        public int minz() {
            return positions.stream().mapToInt(Position3D::z).min().orElseThrow();
        }

        public Brick down() {
            return new Brick(id, positions.stream().map(p -> new Position3D(p.x(), p.y(), p.z() - 1)).toList());
        }

        public Brick up() {
            return new Brick(id, positions.stream().map(p -> new Position3D(p.x(), p.y(), p.z() + 1)).toList());
        }

        public static Brick between(int id, Position3D start, Position3D end) {
            if (start.x() != end.x()) {
                return Brick.between(id, start, end, Position3D::x, x -> new Position3D(x, start.y(), start.z()));
            }
            if (start.y() != end.y()) {
                return Brick.between(id, start, end, Position3D::y, y -> new Position3D(start.x(), y, start.z()));
            }
            if (start.z() != end.z()) {
                return Brick.between(id, start, end, Position3D::z, z -> new Position3D(start.x(), start.y(), z));
            }
            return new Brick(id, List.of(start));
        }

        public static Brick parse(int id, String line) {
            Matcher m = BRICK_PATTERN.matcher(line);
            if (m.matches()) {
                Position3D start = new Position3D(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)), Integer.parseInt(m.group(3)));
                Position3D end = new Position3D(Integer.parseInt(m.group(4)), Integer.parseInt(m.group(5)), Integer.parseInt(m.group(6)));
                return Brick.between(id, start, end);
            }
            throw new IllegalArgumentException(line);
        }

        public String toString() {
            return name() + " [" + positions + "]";
        }
    }

    private static class Space {
        final List<Brick> bricks;
        final Map<Position3D, Brick> positions = new HashMap<>();

        public Space(Space other) {
            this.bricks = new ArrayList<>(other.bricks);
            this.positions.putAll(other.positions);
        }

        public Space(List<String> lines) {
            bricks = new ArrayList<>();
            for (int i = 0; i < lines.size(); i++) {
                bricks.add(Brick.parse(i, lines.get(i)));
            }
            bricks.forEach(b -> b.positions.forEach(p -> positions.put(p, b)));
        }


        void add(Brick brick) {
            bricks.add(brick);
            brick.positions.forEach(p -> positions.put(p, brick));
        }

        void remove(Brick brick) {
            bricks.remove(brick);
            brick.positions.forEach(positions::remove);
        }

        public Brick getBrickAt(Position3D pos) {
            return positions.get(pos);
        }

        public Space stack() {
            List<Brick> ordered = new ArrayList<>(bricks);
            ordered.sort(Comparator.comparing(Brick::minz));
            Space stacked = new Space(this);
            for (Brick b : ordered) {
                stacked.stack(b);
            }
            //Map<Brick, Set<Brick>> supportingBricks = stacked.bricks.stream()
            //        .collect(Collectors.toMap(Function.identity(), stacked::supporting));
            //supportingBricks.entrySet().stream().sorted(Comparator.comparingInt(e -> e.getKey().id()))
            //        .forEach(e -> System.out.println(e.getKey().name() + " is supported by " + e.getValue().stream().sorted(Comparator.comparingInt(Brick::id)).map(b -> b.name()).collect(Collectors.joining(", "))));
            return stacked;
        }

        public void stack(Brick brick) {
            Brick candidate = brick;
            remove(brick);
            while (possible(candidate.down()) && candidate.minz() > 1) {
                candidate = candidate.down();
            }
            add(candidate);
        }

        boolean possible(Brick brick) {
            return brick.positions.stream().noneMatch(positions::containsKey);
        }

        Set<Brick> supportedBy(Brick brick) {
            return brick.positions.stream()
                    .map(p -> new Position3D(p.x(), p.y(), p.z() + 1))
                    .map(positions::get)
                    .filter(Objects::nonNull)
                    .filter(b -> !b.equals(brick))
                    .collect(Collectors.toSet());
        }

        Set<Brick> supporting(Brick brick) {
            return brick.positions.stream()
                    .map(p -> new Position3D(p.x(), p.y(), p.z() - 1))
                    .map(positions::get)
                    .filter(Objects::nonNull)
                    .filter(b -> !b.equals(brick))
                    .collect(Collectors.toSet());
        }

        Collection<Brick> getRemovableBricks() {
            Set<Brick> removable = new HashSet<>();
            Map<Brick, Set<Brick>> supportingBricks = new HashMap<>();
            for (Brick b : bricks) {
                var supported = supportedBy(b);
                if (supported.isEmpty() || supported.stream()
                        .allMatch(s -> supportingBricks.computeIfAbsent(s, this::supporting).size() > 1)) {
                    removable.add(b);
                }
            }
            return removable;
        }

        long countFallingBricks() {
            Map<Brick, Set<Brick>> brickToSupportingBricks = bricks.stream()
                    .collect(Collectors.toMap(Function.identity(), this::supporting));
            Map<Brick, Set<Brick>> brickToSupportedBricks = bricks.stream()
                    .collect(Collectors.toMap(Function.identity(), this::supportedBy));

            Map<Brick, Set<Brick>> fallingBricksByDisintegratedBrick = new HashMap<>();
            for (Brick brick : bricks) {
                fallingBricksByDisintegratedBrick.put(brick, fallIfDisintegrated(brick, brickToSupportedBricks, brickToSupportingBricks));
            }
            return fallingBricksByDisintegratedBrick.values().stream().mapToLong(Set::size).sum();
        }

        public Set<Brick> fallIfDisintegrated(Brick brick, Map<Brick, Set<Brick>> brickToSupportedBricks, Map<Brick, Set<Brick>> brickToSupportingBricks) {
            HashSet<Brick> result = new HashSet<>();
            fallIfDisintegrated(brick, brickToSupportedBricks, brickToSupportingBricks, result);
            return result;
        }

        private void fallIfDisintegrated(Brick b, Map<Brick, Set<Brick>> brickTosupportedBricks, Map<Brick, Set<Brick>> brickToSupportingBricks, Set<Brick> toFall) {
            Set<Brick> supportedBy = brickTosupportedBricks.get(b);
            if (supportedBy.isEmpty()) {
                return;
            }

            var supportedOnlyBy = supportedBy.stream().filter(supported -> {
                var s = new HashSet<>(brickToSupportingBricks.get(supported));
                s.removeAll(toFall);
                s.remove(b);
                return s.isEmpty();
            }).collect(Collectors.toSet());
            toFall.addAll(supportedOnlyBy);
            for (Brick brick : supportedOnlyBy) {
                fallIfDisintegrated(brick, brickTosupportedBricks, brickToSupportingBricks, toFall);
            }
        }
    }


    @Override
    public void run() {
        Space testSpace = new Space(list(getTestInputPath()));
        Space testStacked = testSpace.stack();
        Collection<Brick> testRemovables = testStacked.getRemovableBricks();
        System.out.println("Test Space removables bricks (5) : " + testRemovables.size());
        System.out.println("Test Space falling bricks (7) : " + testStacked.countFallingBricks());

        Space space = new Space(list(getInputPath()));
        Space stacked = space.stack();
        Collection<Brick> removables = stacked.getRemovableBricks();
        System.out.println("Space removables bricks (398) : "+removables.size());
        System.out.println("Space falling bricks (70727) : " + stacked.countFallingBricks());
    }
}

