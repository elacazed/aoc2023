package fr.ela.aoc2023;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.function.LongUnaryOperator;
import java.util.stream.Stream;

public class D05 extends AoC {

    record TTTEntry(long source, long destination, long range) {

        static TTTEntry parse(String entry) {
            String[] numbers = entry.split("\s+");
            return new TTTEntry(Long.parseLong(numbers[1]), Long.parseLong(numbers[0]), Long.parseLong(numbers[2]));
        }

        long to(long from) {
            long offset = from - this.source;
            if (offset < 0 || offset >= range) {
                return -1;
            } else {
                return destination + offset;
            }
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
    }


    @Override
    public void run() {
        Almanac testAlmanac = new Almanac(getTestInputPath());
        System.out.println("Test Lowest Location : "+testAlmanac.getLowestLocation());


        Almanac almanac = new Almanac(getInputPath());
        System.out.println("Lowest Location : "+almanac.getLowestLocation());
    }

}
