package fr.ela.aoc2023;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

public class D12 extends AoC {

    private HashMap<HotSprings, Long> cache = new HashMap<>();

    public record HotSprings(String map, List<Integer> groups) {

        static HotSprings parse(String line) {
            String[] parts = line.split(" ");
            return new HotSprings(parts[0], Arrays.stream(parts[1].split(",")).map(Integer::parseInt).toList());
        }

        HotSprings unfold() {
            List<String> unfoldedMap = new ArrayList<>();
            List<Integer> unfoldedGroups = new ArrayList<>(groups.size() * 5);
            IntStream.range(0, 5).forEach(i -> {
                unfoldedGroups.addAll(groups);
                unfoldedMap.add(map);
            });
            return new HotSprings(String.join("?", unfoldedMap), unfoldedGroups);
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
    }

    long countPermutations(HotSprings hs) {
        if (hs.map.isEmpty()) {
            return hs.groups.isEmpty() ? 1 : 0;
        }
        if (cache.containsKey(hs)) {
            return cache.get(hs);
        }

        char firstChar = hs.map.charAt(0);
        long permutations = switch (firstChar) {
            case '.' -> countPermutations(hs.removeFirstSpring());
            case '?' -> countPermutations(hs.replaceFirstSpring('.')) + countPermutations(hs.replaceFirstSpring('#'));
            case '#' -> hs.maybeRemoveGroup().map(this::countPermutations).orElse(0L);
            default -> throw new IllegalArgumentException();
        };
        cache.put(hs, permutations);
        return permutations;
    }


    @Override
    public void run() {
        List<HotSprings> testSprings = list(getTestInputPath(), HotSprings::parse);
        long testPart1 = testSprings.stream().mapToLong(this::countPermutations).sum();
        System.out.println("Test number of permutations (21): " + testPart1);
        long testPart2 = testSprings.stream().map(HotSprings::unfold).mapToLong(this::countPermutations).sum();
        System.out.println("Test number of unfolded permutations (525152): " + testPart2);

        List<HotSprings> springs = list(getInputPath(), HotSprings::parse);
        long part1 = springs.stream().mapToLong(this::countPermutations).sum();
        System.out.println("Number of permutations (7402): " + part1);
        long part2 = springs.stream().map(HotSprings::unfold).mapToLong(this::countPermutations).sum();
        System.out.println("Number of unfolded permutations (3384337640277): " + part2);
    }


}
