package fr.ela.aoc2023;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.LongUnaryOperator;
import java.util.stream.Stream;

public class D05 extends AoC {

    record Range(long start, long end) implements Comparable<Range> {

        static Optional<Range> maybe(long start, long end) {
            if (end >= start) {
                return Optional.of(new Range(start, end));
            } else {
                return Optional.empty();
            }
        }

        public long size() {
            return end - start + 1;
        }

        public String toString() {
            return "[" + start + ".." + end + "]";
        }

        public boolean contains(long value) {
            return value >= start && value <= end;
        }

        public boolean contains(Range other) {
            return contains(other.start) && contains(other.end);
        }

        public boolean overlap(Range other) {
            return contains(other.start) || contains(other.end) || other.contains(start) || other.contains(end);
        }

        @Override
        public int compareTo(Range o) {
            return Comparator.comparingLong(Range::start).thenComparing(Range::end).compare(this, o);
        }

        public Range intersectionWith(Range other) {
            if (!this.overlap(other)) {
                return null;
            } else if (this.equals(other)) {
                return this;
            } else {
                return new Range(Math.max(this.start, other.start), Math.min(this.end, other.end));
            }
        }

        Range move(long offset) {
            return new Range(start + offset, end + offset);
        }
    }

    static List<Range> reduce(List<Range> ranges) {
        if (ranges.size() == 1 || ranges.isEmpty()) {
            return ranges;
        }
        Set<Range> newRanges = new HashSet<>();
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
        return newRanges.stream().sorted().toList();
    }

    record TTTEntry(Range range, long offset) {

        public String toString() {
            return range + " (" + offset + ")";
        }

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

        public void transform(RangeMap rangeMap) {
            List<Range> remains = new ArrayList<>();
            List<Range> transformations = new ArrayList<>();
            for (Range from : rangeMap.remains) {
                if (range.overlap(from)) {
                    Range intersection = range.intersectionWith(from);
                    Range.maybe(from.start, range.start - 1).ifPresent(remains::add);
                    transformations.add(intersection.move(offset));
                    Range.maybe(range.end + 1, from.end).ifPresent(remains::add);
                } else {
                    remains.add(from);
                }
            }
            rangeMap.remains.clear();
            List<Range> reduced = reduce(remains);
            //if (reduced.size() < remains.size()) {
            //    System.out.println("Reduction of remains : \n\t in : " + remains.stream().sorted().map(Range::toString).collect(Collectors.joining(", ")));
            //    System.out.println("\tout : " + reduced.stream().sorted().map(Range::toString).collect(Collectors.joining(", ")));
            //}
            rangeMap.remains.addAll(reduced);
            rangeMap.transformed.addAll(transformations);
        }
    }

    record RangeMap(List<Range> remains, List<Range> transformed) {
        long size() {
            return transformed.stream().mapToLong(Range::size).sum() + remains.stream().mapToLong(Range::size).sum();
        }

        List<Range> result() {
            var result = new ArrayList<>(transformed);
            result.addAll(remains);
            return result;
        }
    }

    record TTTMap(String name, List<TTTEntry> entriesList) implements LongUnaryOperator {
        static TTTMap parse(String name, Stream<String> stream) {
            return new TTTMap(name, stream.map(TTTEntry::parse).toList());
        }

        @Override
        public long applyAsLong(long thing) {
            if (thing == -1) {
                return -1;
            }
            return entriesList.stream().mapToLong(e -> e.to(thing)).filter(l -> l > -1).findFirst().orElse(thing);
        }

        public List<Range> transform(List<Range> ranges) {
            RangeMap in = new RangeMap(new ArrayList<>(ranges), new ArrayList<>());
            entriesList.forEach(e -> e.transform(in));
            List<Range> out = in.result();
            //System.out.println(name + "\n\tin : " + ranges.stream().sorted().map(Range::toString).collect(Collectors.joining(", ")));
            //System.out.println("\tout : " + out.stream().sorted().map(Range::toString).collect(Collectors.joining(", ")));
            return out;
        }

        public Function<List<Range>, List<Range>> toRanges() {
            return this::transform;
        }
    }

    public class Almanac {
        List<Long> seeds;
        TTTMap seedToSoil, soilToFertilizer, fertilizerToWater, waterToLight, lightToTemperature, temperatureToHumidity, humidityToLocation;

        public Almanac(Path data) {
            List<List<String>> list = splitOnEmptyLines(data);
            seeds = Arrays.stream(list.get(0).get(0).substring("seeds: ".length()).split("\s+")).map(Long::parseLong).toList();

            seedToSoil = parseMap(list.get(1));
            soilToFertilizer = parseMap(list.get(2));
            fertilizerToWater = parseMap(list.get(3));
            waterToLight = parseMap(list.get(4));
            lightToTemperature = parseMap(list.get(5));
            temperatureToHumidity = parseMap(list.get(6));
            humidityToLocation = parseMap(list.get(7));
        }

        static TTTMap parseMap(List<String> lines) {
            String name = lines.get(0).split(" ")[0];
            return TTTMap.parse(name, lines.stream().skip(1));
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
            Function<List<Range>, List<Range>> func = seedToSoil.toRanges()
                    .andThen(soilToFertilizer.toRanges())
                    .andThen(fertilizerToWater.toRanges())
                    .andThen(waterToLight.toRanges())
                    .andThen(lightToTemperature.toRanges())
                    .andThen(temperatureToHumidity.toRanges())
                    .andThen(humidityToLocation.toRanges());
            for (int i = 0; i < seeds.size(); i += 2) {
                long start = seeds.get(i);
                long end = start + seeds.get(i + 1);
                ranges.add(new Range(start, end - 1));
            }
            return func.apply(ranges).stream()
                    .mapToLong(Range::start).min().orElse(-1);
        }

    }


    @Override
    public void run() {
        Almanac testAlmanac = new Almanac(getTestInputPath());
        System.out.println("Test Lowest Location (35) : " + testAlmanac.getLowestLocation());
        System.out.println("Test Lowest Location 2 (46) : " + testAlmanac.getLowestLocationForAllSeeds());

        Almanac almanac = new Almanac(getInputPath());
        System.out.println("Lowest Location (218513636) : " + almanac.getLowestLocation());
        System.out.println("Lowest Location 2 (81956384) : " + almanac.getLowestLocationForAllSeeds());
    }

}
