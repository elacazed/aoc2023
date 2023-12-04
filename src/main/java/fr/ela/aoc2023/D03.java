package fr.ela.aoc2023;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class D03 extends AoC {

    record EngineSchematic(List<PartNumber> partNumbers, List<Symbol> symbols) {
        public int partOne() {
            return partNumbers.stream().filter(pn -> pn.isCloseToSymbol(symbols)).mapToInt(pn -> pn.number).sum();
        }

        public int partTwo() {
            return symbols.stream().map(
                    s -> partNumbers.stream().filter(pn -> pn.isCloseToSymbol(s)).toList())
                    .filter(lpn -> lpn.size() == 2)
                    .mapToInt(lpn -> lpn.stream().mapToInt(pn -> pn.number).reduce((n1, n2) -> n1 * n2).orElse(0))
                    .sum();
        }
    }

    public record Position(int row, int col) {
    }


    public record PartNumber(int number, Position start, int length) {
        boolean isCloseToSymbol(Symbol symbol) {
            int row = start.row;
            if (Math.abs(row - symbol.position.row) <= 1) {
                return symbol.position.col >= (start.col - 1) && symbol.position.col <= (start.col + length);
            }
            return false;
        }

        boolean isCloseToSymbol(List<Symbol> symbol) {
            return symbol.stream().anyMatch(this::isCloseToSymbol);
        }
    }

    public record Symbol(char symbol, Position position) {

    }

    public void parseLine(String line, int row, List<PartNumber> partNumbers, List<Symbol> symbols) {
        int currentPartNumber = -1;
        Position start = null;
        for (int col = 0; col < line.length(); col++) {
            char currentChar = line.charAt(col);
            if (Character.isDigit(currentChar)) {
                if (currentPartNumber == -1) {
                    start = new Position(row, col);
                    currentPartNumber = currentChar - '0';
                } else {
                    currentPartNumber = 10 * currentPartNumber + (currentChar - '0');
                }
            } else {
                if (currentPartNumber != -1) {
                    partNumbers.add(new PartNumber(currentPartNumber, start, col - start.col));
                    currentPartNumber = -1;
                    start = null;
                }
                if (currentChar != '.') {
                    symbols.add(new Symbol(currentChar, new Position(row, col)));
                }
            }
        }
        if (currentPartNumber != -1) {
            partNumbers.add(new PartNumber(currentPartNumber, start, line.length() - start.col));
        }
    }

    public EngineSchematic parseInput(List<String> lines) {
        List<PartNumber> partNumbers = new ArrayList<>();
        List<Symbol> symbols = new ArrayList<>();
        IntStream.range(0, lines.size()).forEach(i -> parseLine(lines.get(i), i, partNumbers, symbols));
        return new EngineSchematic(partNumbers, symbols);
    }

    @Override
    public void run() {
        EngineSchematic test = parseInput(list(getTestInputPath()));
        System.out.println("Test Part Numbers sum : " + test.partOne());
        System.out.println("Test Gear Ratio : " + test.partTwo());
        EngineSchematic schematic = parseInput(list(getInputPath()));
        System.out.println("Part Numbers sum : " + schematic.partOne());
        System.out.println("Gear Ratio : " + schematic.partTwo());
    }


}
