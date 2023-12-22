package fr.ela.aoc2023;

import fr.ela.aoc2023.utils.Grid;
import fr.ela.aoc2023.utils.Position;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class D21 extends AoC {

    public enum Tile {
        ROCK, PLOT, START, ELF;
    }

    public class Garden {
        private Grid<Tile> grid;

        public Garden(List<String> lines) {
            this.grid = Grid.parseCharactersGrid(lines, c -> switch (c) {
                case '#' -> Tile.ROCK;
                case '.' -> Tile.PLOT;
                case 'O' -> Tile.ELF;
                case 'S' -> Tile.START;
                default -> throw new IllegalArgumentException();
            });
        }

        public int getReachablePlots(int steps) {
            Position start = grid.getPositionsOf(Tile.START).stream().reduce((x, y) -> {
                throw new IllegalArgumentException();
            }).orElseThrow();

            List<Position> available = new ArrayList<>(grid.getPositionsOf(Tile.PLOT));
            Set<Position> prev = Set.of(start);
            for (int i = 0; i < steps; i++) {
                Set<Position> next = new HashSet<>();
                for (Position po : prev) {
                    next.addAll(grid.cardinals(po));
                }
                next.retainAll(available);
                for (Position position : next) {
                    grid.put(position, Tile.ELF);
                }
                prev = next;
            }
            return prev.size() + 1;
        }
    }

    @Override
    public void run() {
        Garden testGarden = new Garden(list(getTestInputPath()));
        System.out.println("Plots reached on test garden after 6 steps (16) : " + testGarden.getReachablePlots(6));

        Garden garden = new Garden(list(getInputPath()));
        System.out.println("Plots reached on garden after 64 steps (16) : " + garden.getReachablePlots(64));
    }
}

