package fr.ela.aoc2023;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

public class D01 extends AoC {
    Map<String, Integer> replacements = Map.of("one", 1,
            "two", 2,
            "three", 3,
            "four", 4,
            "five", 5,
            "six", 6,
            "seven", 7,
            "eight", 8,
            "nine", 9);
    Map<Character, List<String>> spelledDigitsByFirstLetter = replacements.keySet().stream()
            .collect(Collectors.groupingBy(digit -> digit.charAt(0)));

    Map<Character, List<String>> spelledDigitsByLastLetter = replacements.keySet().stream()
            .collect(Collectors.groupingBy(digit -> digit.charAt(digit.length() - 1)));

    int getFirstDigitOrSpelledDigit(String line, boolean searchSpelled) {
        for (int i = 0; i < line.length(); i++) {
            char current = line.charAt(i);
            if (Character.isDigit(current)) {
                return current - '0';
            } else if (searchSpelled) {
                Optional<String> spelled = getSpelledDigit(line.substring(i), String::startsWith, spelledDigitsByFirstLetter.get(current));
                if (spelled.isPresent()) {
                    return replacements.get(spelled.get());
                }
            }
        }
        return 0;
    }

    int getLastDigitOrSpelledDigit(String line, boolean searchSpelled) {
        for (int i = line.length() - 1; i >= 0; i--) {
            char current = line.charAt(i);
            if (Character.isDigit(current)) {
                return current - '0';
            } else if (searchSpelled) {
                Optional<String> spelled = getSpelledDigit(line.substring(0, i + 1), String::endsWith, spelledDigitsByLastLetter.get(current));
                if (spelled.isPresent()) {
                    return replacements.get(spelled.get());
                }
            }
        }
        return 0;
    }

    Optional<String> getSpelledDigit(String line, BiPredicate<String, String> predicate, List<String> candidates) {
        if (candidates == null) {
            return Optional.empty();
        }
        return candidates.stream()
                .filter(c -> predicate.test(line, c))
                .findFirst();
    }

    int getNumber(String line, boolean searchSpelled) {
        return getFirstDigitOrSpelledDigit(line, searchSpelled) * 10 + getLastDigitOrSpelledDigit(line, searchSpelled);
    }

    @Override
    public void run() {
        System.out.println("Test Calibration Value 1 : " + stream(getTestInputPath()).mapToInt(line -> getNumber(line, false)).sum());
        System.out.println("Calibration Value 1 : " + stream(getInputPath()).mapToInt(line -> getNumber(line, false)).sum());

        System.out.println("Test Calibration Value 2 : " + stream(getPath("input-test2")).mapToInt(line -> getNumber(line, true)).sum());
        System.out.println("Calibration Value 2 : " + stream(getInputPath()).mapToInt(line -> getNumber(line, true)).sum());
    }


}
