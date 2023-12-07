package fr.ela.aoc2023;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class D06 extends AoC {

    /**
     * Your toy boat has a starting speed of zero millimeters per millisecond.
     * For each whole millisecond you spend at the beginning of the race holding down the button, the boat's speed increases by one millimeter per millisecond.
     *
     * Race : Time allowed, record distance.
     *
     * s : speed
     * d = (time - s) * s
     *
     */

    record Race(long time, long best) {
        List<Long> waysToWin(long timeToSpeedRatio) {
            List<Long> longs = new ArrayList<>();
            for (long hold = 1; hold < time; hold++) {
                long speed = hold * timeToSpeedRatio;
                long d = (time - hold) * speed;
                if (d > best) {
                    longs.add(hold);
                }
            }
            return longs;
        }
    }

    long numberOfWaysToWin(List<Race> races, long timeToSpeedRatio) {
        return races.stream().mapToLong(r -> r.waysToWin(timeToSpeedRatio).size()).reduce(1, (x,y) -> x*y);
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
        System.out.println("Test Number of ways to win part 1 (288) : "+numberOfWaysToWin(testRaces, 1));

        List<Race> races = parseRaces(getInputPath());
        System.out.println("Number of ways to win part 1 (288) : "+numberOfWaysToWin(races, 1));
    }

}
