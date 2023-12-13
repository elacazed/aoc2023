package fr.ela.aoc2023;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class D11 extends AoC {

    @Override
    public void run() {
        Universe test = Universe.parse(list(getTestInputPath()));
        System.out.println("Test universe distances sum (374): "+test.expand().calculate());

        Universe universe = Universe.parse(list(getInputPath()));
        System.out.println("Universe distances sum (9623138): "+universe.expand().calculate());
    }

    public record Galaxy(int id, int x, int y) {
        Galaxy expandX(List<Integer> emptyCols) {
            int nb = (int) emptyCols.stream().filter(c -> c < x).count();
            return new Galaxy(id,x+nb, y);
        }
        Galaxy expandY(List<Integer> emptyLines) {
            int nb = (int) emptyLines.stream().filter(c -> c < y).count();
            return new Galaxy(id, x, y+nb);
        }
    }

    public record Universe(int width, int height, List<Galaxy> galaxies) {

        static Universe parse(List<String> lines) {
            int width = lines.get(0).length();
            int height = lines.size();
            int id = 0;
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

        int calculate() {
            int distance = 0;
            for (Galaxy from : galaxies) {
                for (Galaxy to : galaxies) {
                     distance += Math.abs(to.y-from.y) + Math.abs(to.x-from.x);
                }
            }
            return distance / 2;
        }

        public Universe expand() {
            int h = height;
            int w = width;
            List<Integer> emptyCols = IntStream.range(0, width).filter(col -> galaxies.stream().noneMatch(g -> g.x == col)).boxed().toList();
            var gals = galaxies.stream().map(g -> g.expandX(emptyCols)).toList();
            List<Integer> emptyLines = IntStream.range(0, height).filter(col -> galaxies.stream().noneMatch(g -> g.y == col)).boxed().toList();
            gals = gals.stream().map(g -> g.expandY(emptyLines)).toList();

            return new Universe(w+emptyCols.size(), h+emptyLines.size(), gals);
        }
    }


}
