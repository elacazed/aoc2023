package fr.ela.aoc2023;

import fr.ela.aoc2023.utils.Dijkstra;
import fr.ela.aoc2023.utils.Direction;
import fr.ela.aoc2023.utils.Grid;
import fr.ela.aoc2023.utils.Path;
import fr.ela.aoc2023.utils.Position;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class D17 extends AoC {

    public record Block(Position position, Direction direction, int steps) {
        Block(int x, int y, Direction direction, int steps) {
            this(new Position(x, y), direction, steps);
        }

        List<Block> next() {
            List<Block> blocks = new ArrayList<>();
            if (steps < 2) {
                blocks.add(new Block(direction.move(position), direction, steps + 1));
            }
            blocks.add(new Block(direction.left().move(position), direction.left(), 0));
            blocks.add(new Block(direction.right().move(position), direction.right(), 0));
            return blocks;
        }

        List<Block> ultraNext() {
            List<Block> blocks = new ArrayList<>();
            if (steps < 9) {
                blocks.add(new Block(direction.move(position), direction, steps +1 ));
            }
            if (steps > 2) {
                blocks.add(new Block(direction.left().move(position), direction.left(), 0));
                blocks.add(new Block(direction.right().move(position), direction.right(), 0));
            }
            return blocks;
        }
    }

    public static class City {
        private final Grid<Long> heatLossGrid;

        public City(List<String> lines) {
            heatLossGrid = Grid.parseCharactersGrid(lines, c -> c == '.' ? null : (long) (c - '0'));
        }

        public boolean isExit(Block block) {
            return heatLossGrid.isBottomRightCorner(block.position);
        }

        public Path<Block, Long> findLessHeatLosingPath(boolean ultra) {
            Function<Block, List<Block>> nextNodes = ultra ? Block::ultraNext : Block::next;
            nextNodes = nextNodes.andThen(l -> l.stream().filter(b -> heatLossGrid.inBounds(b.position)).toList());

            Dijkstra<Block, Long> dijkstra = Dijkstra.longDijkstra(nextNodes, n -> heatLossGrid.get(n.position()));
            List<Block> starts = List.of(
                    new Block(1, 0, Direction.EAST, 0),
                    new Block(0, 1, Direction.SOUTH, 0));
            return dijkstra.findShortestPath(starts, this::isExit);
        }

        public String draw(List<Block> path) {
            List<char[]> lines = heatLossGrid.draw(l -> '.');
            path.forEach(n -> {
                char c = switch (n.direction()) {
                    case EAST -> '>';
                    case WEST -> '<';
                    case SOUTH -> 'v';
                    case NORTH -> '^';
                };
                lines.get((int) n.position.y())[(int) n.position.x()] = c;
            });
            return lines.stream().map(String::new).collect(Collectors.joining("\n"));
        }
    }

    @Override
    public void run() {
        City testCity = new City(list(getTestInputPath()));
        Long time = System.currentTimeMillis();
        Path<Block, Long> testPath = testCity.findLessHeatLosingPath(false);
        time = System.currentTimeMillis() - time;
        System.out.println("Test Cave best path (102) : " + testPath.cost()+" (+"+time+" ms)");
        time = System.currentTimeMillis();
        testPath = testCity.findLessHeatLosingPath(true);
        time = System.currentTimeMillis() - time;
        System.out.println("Test Cave best path (94) : " + testPath.cost()+" (+"+time+" ms)");

        City city = new City(list(getInputPath()));
        time = System.currentTimeMillis();
        Path<Block, Long> path = city.findLessHeatLosingPath(false);
        time = System.currentTimeMillis() - time;
        System.out.println("Cave best path (817) : " + path.cost()+" (+"+time+" ms)");
        time = System.currentTimeMillis();
        path = city.findLessHeatLosingPath(true);
        time = System.currentTimeMillis() - time;
        System.out.println("Cave best path (925) : " + path.cost()+" (+"+time+" ms)");
    }
}

