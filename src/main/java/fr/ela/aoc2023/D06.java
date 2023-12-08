package fr.ela.aoc2023;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class D06 extends AoC {

    /**
     * Race : Time allowed, record distance.
     * With:
     *  T = time allowed,
     *  D = record distance
     *  x = time holding the button,
     *  The distance d wan be expressed as : d = (T - x) * x
     *
     *  We are looking for x such as :
     *     x is an integer, and :
     *     D < (T -x) * x
     *
     *  => x² - Tx + D < 0
     *  So we need to count integers between the 2 roots (excluded) of the 2nd degree polynomial function
     *
     * Delta = T² - 4 D
     * r1 = (T - sqrt(delta)) / 2
     * r2 = (T + sqrt(delta)) / 2
     */

    record Race(long time, long best) {
        long waysToWin() {
            double delta = (double) (time * time) - 4 * best;
            double r1 = (time - Math.sqrt(delta)) / 2;
            double r2 = (time + Math.sqrt(delta)) / 2;
            // Lowest root : we add 1 to the floor to be sure to exclude the root if it is an integer.
            long nextIntegerGreaterThanR1 = (long) Math.floor(r1) + 1;
            // Highest root, which must be excluded if it is an integer.
            long r2floor = (long) Math.floor(r2);
            long integerLowerThanR2 = r2 == r2floor ? r2floor - 1 : r2floor;
            return integerLowerThanR2 - nextIntegerGreaterThanR1 + 1;
        }

        static Race longRace(List<Race> races) {
            long time = Long.parseLong(races.stream().map(race -> Long.toString(race.time)).collect(Collectors.joining()));
            long best = Long.parseLong(races.stream().map(race -> Long.toString(race.best)).collect(Collectors.joining()));
            return new Race(time, best);
        }
    }

    long numberOfWaysToWin(List<Race> races) {
        return races.stream().mapToLong(Race::waysToWin).reduce(1, (x, y) -> x * y);
    }

    List<Race> parseRaces(Path path) {
        List<String> lines = list(path);
        String[] times = lines.get(0).substring("Time:".length()).trim().split("\s+");
        String[] distances = lines.get(1).substring("Distance:".length()).trim().split("\s+");
        return IntStream.range(0, times.length).mapToObj(i -> new Race(Long.parseLong(times[i]), Long.parseLong(distances[i]))).toList();
    }

    @Override
    public void run() {
        List<Race> testRaces = parseRaces(getTestInputPath());
        System.out.println("Test Number of ways to win part 1 (288) : " + numberOfWaysToWin(testRaces));
        System.out.println("Test Number of ways to win part 2 (71503) : " + Race.longRace(testRaces).waysToWin());

        List<Race> races = parseRaces(getInputPath());
        System.out.println("Number of ways to win part 1 (345015) : " + numberOfWaysToWin(races));
        System.out.println("Number of ways to win part 2 (42588603) : " + Race.longRace(races).waysToWin());
    }

}
