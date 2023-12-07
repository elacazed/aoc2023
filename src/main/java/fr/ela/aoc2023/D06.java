package fr.ela.aoc2023;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class D06 extends AoC {

    /**
     * Your toy boat has a starting speed of zero millimeters per millisecond.
     * For each whole millisecond you spend at the beginning of the race holding down the button, the boat's speed increases by one millimeter per millisecond.
     * <p>
     * Race : Time allowed, record distance.
     * <p>
     * s : speed
     * d = (time - s) * s
     * <p>
     * 2 -> 5
     * 4 -> 11
     * 11 -> 19
     */

    record Race(long time, long best) {
        long waysToWin() {
            double delta = (time * time) - 4 * best;
            long r1 = (long) Math.floor((time - Math.sqrt(delta)) / 2) + 1;

            double r2Exact = (time + Math.sqrt(delta)) / 2;
            double r2Floor = Math.floor(r2Exact);
            long r2 = (long) (r2Floor == r2Exact ? r2Floor - 1 : r2Floor);
            long result = r2 - r1 + 1;
            return result;
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
