package fr.ela.aoc2023;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class D04 extends AoC {

    static Pattern CARD_PATTERN = Pattern.compile("Card\s+([0-9]+): ([0-9 ]+)\\|([0-9 ]+)");

    public record Card(int index, List<Integer> winning, List<Integer> actual) {

        public static Card parse(String line) {
            Matcher matcher = CARD_PATTERN.matcher(line);
            matcher.matches();
            return new Card(Integer.parseInt(matcher.group(1)), parseNumbers(matcher.group(2)), parseNumbers(matcher.group(3)));
        }

        private static List<Integer> parseNumbers(String numbers) {
            return Arrays.stream(numbers.split("\s")).filter(s -> ! s.isEmpty()).map(Integer::parseInt).toList();
        }

        public int scorePartOne() {
            var copy = new ArrayList<>(actual);
            copy.retainAll(winning);
            return copy.isEmpty() ? 0 : (int) Math.pow(2, copy.size() - 1);
        }

    }


    @Override
    public void run() {
        System.out.println("Test points part 1 : "+stream(getTestInputPath()).map(Card::parse).mapToInt(Card::scorePartOne).sum());
        System.out.println("Points part 1 : "+stream(getInputPath()).map(Card::parse).mapToInt(Card::scorePartOne).sum());
    }


}
