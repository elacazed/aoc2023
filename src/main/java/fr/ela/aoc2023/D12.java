package fr.ela.aoc2023;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


public class D12 extends AoC {

    public record HotSprings(String map, List<Integer> groups) {

        static HotSprings parse(String line) {
            String[] parts = line.split(" ");
            return new HotSprings(parts[0], Arrays.stream(parts[1].split(",")).map(Integer::parseInt).toList());
        }

        HotSprings removeFirstSpring() {
            return new HotSprings(map.substring(1), groups);
        }

        HotSprings replaceFirstSpring(char c) {
            return new HotSprings(c + map.substring(1), groups);
        }

        Optional<HotSprings> maybeRemoveGroup() {
            if (groups.isEmpty()) {
                return Optional.empty();
            }
            int nb = groups.get(0);
            if (map.length() < nb || map.chars().limit(nb).anyMatch(c -> c == '.')) {
                return Optional.empty();
            }
            if (nb == map.length()) {
                if (groups.size() == 1) {
                    return Optional.of(new HotSprings("", List.of()));
                } else {
                    return Optional.empty();
                }
            }
            if (map.charAt(nb) == '#') {
                return Optional.empty();
            }
            return Optional.of(new HotSprings(map.substring(nb + 1), groups.subList(1, groups.size())));
        }

        long countPermutations() {
            if (map.isEmpty()) {
                return groups.isEmpty() ? 1 : 0;
            }

            char firstChar = map.charAt(0);
            return switch (firstChar) {
                case '.' -> removeFirstSpring().countPermutations();
                case '?' -> replaceFirstSpring('.').countPermutations() + replaceFirstSpring('#').countPermutations();
                case '#' -> maybeRemoveGroup().map(HotSprings::countPermutations).orElse(0L);
                default -> throw new IllegalArgumentException();
            };
        }
    }



    @Override
    public void run() {
        long testPart1 = list(getTestInputPath(), HotSprings::parse).stream().mapToLong(HotSprings::countPermutations).sum();
        System.out.println("Test number of permutations (21): " + testPart1);

        long part1 = list(getInputPath(), HotSprings::parse).stream().mapToLong(HotSprings::countPermutations).sum();
        System.out.println("Number of permutations (7402): " + part1);
    }


}
