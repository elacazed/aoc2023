package fr.ela.aoc2023;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.ToIntBiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    int getFirstDigit(String line, IntStream intStream, ToIntBiFunction<String, Integer> mapper) {
        return intStream.map(i -> mapper.applyAsInt(line, i)).filter(n -> n > 0).findFirst().orElse(0);
    }

    ToIntBiFunction<String, Integer> digitFinder = (line, index) -> {
        char current = line.charAt(index);
        if (Character.isDigit(current)) {
            return current - '0';
        } else {
            return 0;
        }
    };

    ToIntBiFunction<String, Integer> firstSpelledDigitFinder = (line, index) -> {
        int digit = digitFinder.applyAsInt(line, index);
        if (digit == 0) {
            digit = getSpelledDigit(line.substring(index), String::startsWith, spelledDigitsByFirstLetter.get(line.charAt(index)))
                    .map(replacements::get)
                    .orElse(0);
        }
        return digit;
    };

    ToIntBiFunction<String, Integer> lastSpelledDigitFinder = (line, index) -> {
        int digit = digitFinder.applyAsInt(line, index);
        if (digit == 0) {
            digit = getSpelledDigit(line.substring(0, index + 1), String::endsWith, spelledDigitsByLastLetter.get(line.charAt(index)))
                    .map(replacements::get)
                    .orElse(0);
        }
        return digit;
    };


    Optional<String> getSpelledDigit(String line, BiPredicate<String, String> predicate, List<String> candidates) {
        if (candidates == null) {
            return Optional.empty();
        }
        return candidates.stream()
                .filter(c -> predicate.test(line, c))
                .findFirst();
    }

    int getNumber(String line, ToIntBiFunction<String, Integer> first, ToIntBiFunction<String, Integer> last) {
        return getFirstDigit(line, IntStream.range(0, line.length()), first) * 10 + getFirstDigit(line, reverseRange(0, line.length()), last);
    }
    int getNumberPartOne(String line) {
        return getNumber(line, digitFinder, digitFinder);
    }

    int getNumberPartTwo(String line) {
        return getNumber(line, firstSpelledDigitFinder, lastSpelledDigitFinder);
    }

    @Override
    public void run() {
        System.out.println("Test Calibration Value 1 : " + stream(getTestInputPath()).mapToInt(this::getNumberPartOne).sum());
        System.out.println("Calibration Value 1 : " + stream(getInputPath()).mapToInt(this::getNumberPartOne).sum());

        System.out.println("Test Calibration Value 2 : " + stream(getPath("input-test2")).mapToInt(this::getNumberPartTwo).sum());
        System.out.println("Calibration Value 2 : " + stream(getInputPath()).mapToInt(this::getNumberPartTwo).sum());
    }


}
