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
            heatLossGrid = Grid.parseCharactersGrid(lines, c -> (long) (c - '0'));
        }

        public List<Block> getCrucibleNextNodes(Block from) {
            return from.next().stream().filter(n -> heatLossGrid.inBounds(n.position)).toList();
        }

        public List<Block> getUltraCrucibleNextNodes(Block from) {
            return from.ultraNext().stream().filter(n -> heatLossGrid.inBounds(n.position)).toList();
        }

        public boolean isExit(Block block) {
            return heatLossGrid.isBottomRightCorner(block.position);
        }

        public Path<Block, Long> findLessHeatLosingPath(boolean ultra) {
            Function<Block, List<Block>> nextNodes = ultra ? Block::ultraNext : Block::next;
            nextNodes = nextNodes.andThen(l -> l.stream().filter(b -> heatLossGrid.inBounds(b.position)).toList());

            Dijkstra<Block, Long> dijkstra = Dijkstra.longDijkstra(nextNodes, n -> heatLossGrid.get(n.position));
            List<Block> starts = List.of(
                    new Block(1, 0, Direction.EAST, 1),
                    new Block(0, 1, Direction.SOUTH, 1));
            return dijkstra.findShortestPath(starts, this::isExit);
        }

        public String draw(List<Block> path) {
            List<char[]> lines = heatLossGrid.draw(l -> '.');
            path.forEach(n -> {
                char c = switch (n.direction()) {
                    case EAST -> '>';
                    case WEST -> '<';
                    case SOUTH -> 'V';
                    case NORTH -> '^';
                };
                lines.get(n.position.y())[n.position.x()] = c;
            });
            return lines.stream().map(String::new).collect(Collectors.joining("\n"));
        }
    }

    @Override
    public void run() {
        City testCity = new City(list(getTestInputPath()));
        Path<Block, Long> testPath = testCity.findLessHeatLosingPath(false);
        System.out.println("Test Cave best path (102) : " + testPath.cost());
        testPath = testCity.findLessHeatLosingPath(true);
        System.out.println("Test Cave best path (94) : " + testPath.cost());

        City city = new City(list(getInputPath()));
        Path<Block, Long> path = city.findLessHeatLosingPath(false);
        System.out.println("Cave best path (817) : " + path.cost());
        path = city.findLessHeatLosingPath(true);
        System.out.println("Cave best path (925) : " + path.cost());
    }
}

