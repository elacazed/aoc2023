package fr.ela.aoc2023;

import com.sun.source.tree.BreakTree;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

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
        Long waysToWin(long timeToSpeedRatio) {
            double delta = (time * time) - 4 * best;
            long r1 = (long) Math.floor((time - Math.sqrt(delta)) / 2) + 1;

            double r2Exact = (time + Math.sqrt(delta)) / 2;
            double r2Floor = Math.floor(r2Exact);
            long r2 = (long) (r2Floor == r2Exact ? r2Floor - 1 : r2Floor);
            long result = r2 - r1 + 1;
            return result;
        }
    }

    long numberOfWaysToWin(List<Race> races, long timeToSpeedRatio) {
        return races.stream().mapToLong(r -> r.waysToWin(timeToSpeedRatio)).reduce(1, (x, y) -> x * y);
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
        System.out.println("Test Number of ways to win part 1 (288) : " + numberOfWaysToWin(testRaces, 1));

        List<Race> races = parseRaces(getInputPath());
        System.out.println("Number of ways to win part 1 (345015) : " + numberOfWaysToWin(races, 1));
    }

}
