package fr.ela.aoc2023.utils;

import fr.ela.aoc2023.D16;
import fr.ela.aoc2023.D17;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class Grid<N> {

    Map<Position, N> map;
    final int width;
    final int height;

    public Grid(int width, int height) {
        this.width = width;
        this.height = height;
        map = new HashMap<>();
    }

    public static <K> Grid<K> parseCharactersGrid(List<String> lines, Function<Character, K> mapper) {
        int height = lines.size();
        int width = lines.get(0).length();
        Grid<K> grid = new Grid<>(width, height);
        for (int y = 0; y < height; y++) {
            String line = lines.get(y);
            for (int x = 0; x < width; x++) {
                char c = line.charAt(x);
                if (c != '.') {
                    grid.map.put(new Position(x, y), mapper.apply(line.charAt(x)));
                }
            }
        }
        return grid;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public N get(int x, int y) {
        return get(new Position(x, y));
    }

    public N get(Position pos) {
        return map.get(pos);
    }

    public boolean inBounds(Position pos) {
        return pos.x() >= 0 && pos.y() >= 0 && pos.x() < width && pos.y() < height;
    }

    public boolean isBottomRightCorner(Position position) {
        return position.y() == height - 1 && position.x() == width - 1;
    }

    public List<char[]> draw(Function<N, Character> mapper) {
        List<char[]> sb = new ArrayList<>();
        for (int y = 0; y < height; y++) {
            char[] line = new char[width];
            for (int x = 0; x < width; x++) {
                line[x] = mapper.apply(get(x, y));
            }
            sb.add(line);
        }
        return sb;
    }
}