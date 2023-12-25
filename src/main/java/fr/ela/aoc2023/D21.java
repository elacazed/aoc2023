package fr.ela.aoc2023;

import fr.ela.aoc2023.utils.Direction;
import fr.ela.aoc2023.utils.Grid;
import fr.ela.aoc2023.utils.Position;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class D21 extends AoC {

    public enum Tile {
        ROCK, PLOT, START, ELF;
    }

    public class Garden {
        private Grid<Tile> grid;

        final Position start;

        public Garden(List<String> lines) {
            this.grid = Grid.parseCharactersGrid(lines, c -> switch (c) {
                case '#' -> Tile.ROCK;
                case '.' -> null;
                case 'S' -> Tile.START;
                default -> throw new IllegalArgumentException();
            });
            start = grid.getPositionsOf(Tile.START).stream().reduce((x, y) -> {
                throw new IllegalArgumentException();
            }).orElseThrow();
            grid.remove(start);
        }

        public long getReachablePlots(long steps) {
            Set<Position> prev = Set.of(start);
            for (long i = 0; i < steps; i++) {
                prev = prev.stream().flatMap(p -> grid.cardinalsIf(p, pos -> !grid.contains(pos)).stream()).collect(Collectors.toSet());
            }
            return prev.size();
        }

        Position mod(Position pos) {
            int x = ((pos.x() % grid.getWidth()) + grid.getWidth()) % grid.getWidth();
            int y = ((pos.y() % grid.getHeight()) + grid.getHeight()) % grid.getHeight();
            return new Position(x, y);
        }

        public long getReachablePlots2() {
            List<Long> totals = new ArrayList<>();
            List<Long> deltas = new ArrayList<>();
            List<Long> deltaDeltas = new ArrayList<>();
            Map<Position, Integer> distances = new HashMap<>();
            distances.put(start, 0);
            long totalReached = 0;
            int index = 0;
            List<Position> reachablePoints = new ArrayList<>();
            reachablePoints.add(start);
            while (index < 1000) {
                index++;
                List<Position> tmp2 = new ArrayList<>();
                for (Position c : reachablePoints) {
                    for (Direction direction : Direction.values()) {
                        Position candidate = direction.move(c);
                        if (distances.get(candidate) == null) {
                            Position mod = mod(candidate);
                            if (!grid.contains(mod)) {
                                tmp2.add(candidate);
                                distances.put(candidate, index);
                            }
                        }
                    }
                }
                if (index % 2 == 1) {
                    totalReached += tmp2.size();
                    if (index % 262 == 65) {
                        totals.add(totalReached);
                        int currTotals = totals.size();
                        if (currTotals > 1) {
                            deltas.add(totals.get(currTotals - 1) - totals.get(currTotals - 2));
                        }
                        int currDeltas = deltas.size();
                        if (currDeltas > 1) {
                            deltaDeltas.add(deltas.get(currDeltas - 1) - deltas.get(currDeltas - 2));
                        }
                        if (deltaDeltas.size() > 1) {
                            break;
                        }
                    }

                }
                reachablePoints = tmp2;
            }
            long neededLoopCount = 26501365 / 262 - 1;
            long currentLoopCount = index / 262 - 1;
            long deltaLoopCount = neededLoopCount - currentLoopCount;
            long deltaLoopCountTriangular = (neededLoopCount * (neededLoopCount + 1)) / 2 - (currentLoopCount * (currentLoopCount + 1)) / 2;
            long deltaDelta = deltaDeltas.get(deltaDeltas.size() - 1);
            long initialDelta = deltas.get(0);
            return (deltaDelta * deltaLoopCountTriangular + initialDelta * deltaLoopCount + totalReached);
        }

    }

    @Override
    public void run() {
        Garden testGarden = new Garden(list(getTestInputPath()));
        System.out.println("Plots reached on test garden after 6 steps (16) : " + testGarden.getReachablePlots(6));

        Garden garden = new Garden(list(getInputPath()));
        System.out.println("Plots reached on garden after 64 steps (3639) : " + garden.getReachablePlots(64));
        System.out.println("Part 2 (no way I would have find this myself) : " + garden.getReachablePlots2());
    }
}

