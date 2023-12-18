package fr.ela.aoc2023;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.LongStream;

public class D11 extends AoC {

    @Override
    public void run() {
        Universe test = Universe.parse(list(getTestInputPath()));
        System.out.println("Test universe distances sum (374): " + test.expand(2).calculate());
        System.out.println("Test universe distances sum (1030): " + test.expand(10).calculate());
        System.out.println("Test universe distances sum (8410): " + test.expand(100).calculate());

        Universe universe = Universe.parse(list(getInputPath()));
        System.out.println("Universe distances sum (9623138): " + universe.expand(2).calculate());
        System.out.println("Universe distances sum (726820169514): " + universe.expand(1_000_000).calculate());
    }

    public record Galaxy(long id, long x, long y) {
        Galaxy expandX(long ratio, List<Long> emptyCols) {
            long nb = (long) emptyCols.stream().filter(c -> c < x).count();
            return new Galaxy(id, x + nb * (ratio -1), y);
        }

        Galaxy expandY(long ratio, List<Long> emptyLines) {
            long nb = (long) emptyLines.stream().filter(c -> c < y).count();
            return new Galaxy(id, x, y + nb * (ratio - 1));
        }
    }

    public record Universe(long width, long height, List<Galaxy> galaxies) {

        static Universe parse(List<String> lines) {
            long width = lines.get(0).length();
            long height = lines.size();
            long id = 0;
            var gals = new ArrayList<Galaxy>();
            for (int y = 0; y < height; y++) {
                String line = lines.get(y);
                for (int x = 0; x < width; x++) {
                    if (line.charAt(x) == '#') {
                        gals.add(new Galaxy(id, x, y));
                        id++;
                    }
                }
            }
            return new Universe(width, height, gals);
        }

        long calculate() {
            long distance = 0;
            for (Galaxy from : galaxies) {
                for (Galaxy to : galaxies) {
                    distance += Math.abs(to.y - from.y) + Math.abs(to.x - from.x);
                }
            }
            return distance / 2;
        }

        // ratio is the final nu;be of empty lines after expansion => 2 for part 1.
        public Universe expand(long ratio) {
            long h = height;
            long w = width;
            List<Long> emptyCols = LongStream.range(0, width).filter(col -> galaxies.stream().noneMatch(g -> g.x == col)).boxed().toList();
            var gals = galaxies.stream().map(g -> g.expandX(ratio, emptyCols)).toList();
            List<Long> emptyLines = LongStream.range(0, height).filter(col -> galaxies.stream().noneMatch(g -> g.y == col)).boxed().toList();
            gals = gals.stream().map(g -> g.expandY(ratio, emptyLines)).toList();

            return new Universe(w + emptyCols.size(), h + emptyLines.size(), gals);
        }
    }


}
