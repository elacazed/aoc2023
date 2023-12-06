package fr.ela.aoc2023;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.function.LongUnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class D05 extends AoC {

    record Range(long start, long end) implements Comparable<Range> {

        public Range(long start, long end) {
            this.start = start;
            this.end = end;
        }

        public boolean contains(long value) {
            return value >= start && value <= end;
        }

        public boolean contains(Range other) {
            return contains(other.start) && contains(other.end);
        }

        public boolean overlap(Range other) {
            return contains(other.start) || contains(other.end);
        }

        @Override
        public int compareTo(Range o) {
            return Comparator.comparingLong(Range::start).thenComparing(Range::end).compare(this, o);
        }

        Range move(long offset) {
            return new Range(start + offset, end + offset);
        }
    }

    static List<Range> reduce(List<Range> ranges) {
        if (ranges.size() == 1 || ranges.isEmpty()) {
            return ranges;
        }
        List<Range> newRanges = new ArrayList<>();
        Iterator<Range> it = ranges.stream().sorted(Comparator.comparingLong(Range::start)).iterator();
        Range current = it.next();
        long curStart = current.start;
        long currEnd = current.end;
        while (it.hasNext()) {
            current = it.next();
            if (current.start > currEnd + 1) {
                newRanges.add(new Range(curStart, currEnd));
                curStart = current.start;
                currEnd = current.end;
            } else {
                currEnd = Math.max(currEnd, current.end);
            }
        }
        newRanges.add(new Range(curStart, currEnd));
        return newRanges;
    }


    record TTTEntry(Range range, long offset) {

        static TTTEntry parse(String entry) {
            String[] numbers = entry.split("\s+");
            long source = Long.parseLong(numbers[1]);
            long destination = Long.parseLong(numbers[0]);
            long offset = destination - source;

            return new TTTEntry(new Range(source, source + Long.parseLong(numbers[2]) - 1), offset);
        }

        long to(long from) {
            if (range.contains(from)) {
                return from + offset;
            } else {
                return -1;
            }
        }

        List<Range> to(Range from) {
            List<Range> ranges = new ArrayList<>();
            if (range.contains(from)) {
                ranges.add(new Range(from.start + offset, from.end + offset));
            } else if (range.overlap(from) || from.overlap(range)) {
                if (from.start < range.start) {
                    Range before = new Range(from.start, range.start - 1);
                    ranges.add(before);
                }
                Range overlap = new Range(Math.max(from.start, range.start), Math.min(from.end, range.end));
                ranges.add(overlap.move(offset));
                if (from.end > range.end) {
                    var after = new Range(range.end + 1, from.end);
                    ranges.add(after);
                }
            }
//            System.out.println(this+ " : \n\tin  : "+from+
//                    "\n\tout : "+ranges.stream().sorted().map(Range::toString).collect(Collectors.joining(", ")));
            return ranges;

        }

        List<Range> to(List<Range> from) {
            var out = from.stream().map(this::to).flatMap(List::stream).toList();
            return out;
        }

    }

    record TTTMap(String name, List<TTTEntry> entriesList) implements LongUnaryOperator {
        static TTTMap parse(String name, Stream<String> stream) {
            return new TTTMap(name, stream.map(TTTEntry::parse).toList());
        }

        long getThing(long thing) {
            if (thing == -1) {
                return -1;
            }
            return entriesList.stream().mapToLong(e -> e.to(thing)).filter(l -> l > -1).findFirst().orElse(thing);
        }

        @Override
        public long applyAsLong(long operand) {
            return getThing(operand);
        }

        public List<Range> toRanges(List<Range> ranges) {
            var out = entriesList.stream().flatMap(e -> e.to(ranges).stream()).toList();
            out = out.isEmpty() ? ranges : out;
            var result = reduce(out);
            System.out.println(name+ " : out : "+out.stream().sorted().map(Range::toString).collect(Collectors.joining(", ")));
            if (result.size() != out.size()) {
                System.out.println(name + " : red : " + result.stream().sorted().map(Range::toString).collect(Collectors.joining(", ")));
            }
            return out;
        }

        public Function<List<Range>, List<Range>> toRanges() {
            return this::toRanges;
        }
    }

    public class Almanac {
        List<Long> seeds;
        TTTMap seedToSoil;
        TTTMap soilToFertilizer;
        TTTMap fertilizerToWater;
        TTTMap waterToLight;
        TTTMap lightToTemperature;
        TTTMap temperatureToHumidity;
        TTTMap humidityToLocation;

        public Almanac(Path data) {
            List<List<String>> list = splitOnEmptyLines(data);
            seeds = Arrays.stream(list.get(0).get(0).substring("seeds: ".length()).split("\s+")).map(Long::parseLong).toList();

            seedToSoil = TTTMap.parse(list.get(1).get(0), list.get(1).stream().skip(1));
            soilToFertilizer = TTTMap.parse(list.get(2).get(0), list.get(2).stream().skip(1));
            fertilizerToWater = TTTMap.parse(list.get(3).get(0), list.get(3).stream().skip(1));
            waterToLight = TTTMap.parse(list.get(4).get(0), list.get(4).stream().skip(1));
            lightToTemperature = TTTMap.parse(list.get(5).get(0), list.get(5).stream().skip(1));
            temperatureToHumidity = TTTMap.parse(list.get(7).get(0), list.get(6).stream().skip(1));
            humidityToLocation = TTTMap.parse(list.get(7).get(0), list.get(7).stream().skip(1));
        }

        long getLowestLocation() {
            LongUnaryOperator func = l -> seedToSoil
                    .andThen(soilToFertilizer)
                    .andThen(fertilizerToWater)
                    .andThen(waterToLight)
                    .andThen(lightToTemperature)
                    .andThen(temperatureToHumidity)
                    .andThen(humidityToLocation).applyAsLong(l);
            return seeds.stream().mapToLong(Long::longValue).map(func).filter(l -> l != -1).min().orElse(-1);
        }

        long getLowestLocationForAllSeeds() {
            List<Range> ranges = new ArrayList<>();
            for (int i = 0; i < seeds.size(); i += 2) {
                long start = seeds.get(i);
                long end = start + seeds.get(i + 1) - 1;
                ranges.add(new Range(start, end));
            }
            Function<Range, List<Range>> func = r -> seedToSoil.toRanges()
                    .andThen(soilToFertilizer.toRanges())
                    .andThen(fertilizerToWater.toRanges())
                    .andThen(waterToLight.toRanges())
                    .andThen(lightToTemperature.toRanges())
                    .andThen(temperatureToHumidity.toRanges())
                    .andThen(humidityToLocation.toRanges()).apply(List.of(r));

            long min = ranges.stream().map(func).flatMap(List::stream).mapToLong(Range::start).min().orElse(-1);
            return min;
        }
    }


    @Override
    public void run() {
        Almanac testAlmanac = new Almanac(getTestInputPath());
        System.out.println("Test Lowest Location (35) : " + testAlmanac.getLowestLocation());
        System.out.println("Test Lowest Location 2 (46) : " + testAlmanac.getLowestLocationForAllSeeds());

       Almanac almanac = new Almanac(getInputPath());
       System.out.println("Lowest Location (218513636) : " + almanac.getLowestLocation());
       System.out.println("Lowest Location 2 : " + almanac.getLowestLocationForAllSeeds());
    }

}
