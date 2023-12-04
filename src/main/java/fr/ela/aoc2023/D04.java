package fr.ela.aoc2023;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
            return Arrays.stream(numbers.split("\s")).filter(s -> !s.isEmpty()).map(Integer::parseInt).toList();
        }

        public int scorePartOne() {
            var matches = countMatching();
            return matches == 0 ? 0 : (int) Math.pow(2, matches - 1);
        }

        public int countMatching() {
            var copy = new ArrayList<>(actual);
            copy.retainAll(winning);
            return copy.size();
        }
    }

    public Map<Card, Integer> countCopies(List<Card> cards) {
        Map<Card, Integer> map = new HashMap<>();
        cards.forEach(c -> map.put(c, 1));

        for (int i = 0; i < cards.size(); i++) {
            Card c = cards.get(i);
            int cardCount = map.get(c);
            for (int j = 1; j <= c.countMatching(); j++) {
                map.computeIfPresent(cards.get(i+j), (card,p) -> p+cardCount);
            }
        }
        return map;
    }

    @Override
    public void run() {
        List<Card> testCards = list(getTestInputPath(), Card::parse);
        System.out.println("Test points part 1 : " + testCards.stream().mapToInt(Card::scorePartOne).sum());
        System.out.println("Test number of cards : : " + countCopies(testCards).values().stream().mapToInt(Integer::intValue).sum());

        List<Card> cards = list(getInputPath(), Card::parse);
        System.out.println("Points part 1 : " + cards.stream().mapToInt(Card::scorePartOne).sum());
        System.out.println("Number of cards : : " + countCopies(cards).values().stream().mapToInt(Integer::intValue).sum());
    }


}
