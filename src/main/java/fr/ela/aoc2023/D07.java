package fr.ela.aoc2023;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class D07 extends AoC {


    public record Hand(String cards, Map<Character, Integer> buckets) implements Comparable<Hand> {

        public Hand(String cards) {
            this(cards, new HashMap<>());
            fillBuckets();
            //System.out.println(cards+" : "+getHandType());
        }

        private void fillBuckets() {
            for (char c : cards.toCharArray()) {
                buckets.merge(c, 1, (value, count) -> value + 1);
            }
        }

        HandType getHandType() {
            return switch (buckets.size()) {
                case 1 -> HandType.FIVE_OF_A_KIND;
                case 2 -> buckets.containsValue(4) ? HandType.FOUR_OF_A_KIND : HandType.FULL_HOUSE;
                case 3 -> buckets.containsValue(3) ? HandType.THREE_OF_A_KIND : HandType.TWO_PAIR;
                case 4 -> HandType.ONE_PAIR;
                default -> HandType.HIGH_CARD;
            };
        }

        int cardValue(int index) {
            char c = cards.charAt(index);
            return switch (c) {
                case 'A' -> 14;
                case 'K' -> 13;
                case 'Q' -> 12;
                case 'J' -> 11;
                case 'T' -> 10;
                default -> c - '0';
            };
        }

        @Override
        public int compareTo(Hand o) {
            int typeComparison = getHandType().compareTo(o.getHandType());
            if (typeComparison == 0) {
                return IntStream.range(0, cards.length())
                        .map(index -> cardValue(index) - o.cardValue(index))
                        .filter(v -> v != 0)
                        .findFirst().orElse(0);
            }
            return typeComparison;
        }
    }

    public enum HandType {
        // Ordinal is important!
        HIGH_CARD,
        ONE_PAIR,
        TWO_PAIR,
        THREE_OF_A_KIND,
        FULL_HOUSE,
        FOUR_OF_A_KIND,
        FIVE_OF_A_KIND;
    }

    record Bid(Hand hand, int bid) {
        static Bid parse(String line) {
            String[] args = line.split(" ");
            return new Bid(new Hand(args[0]), Integer.parseInt(args[1]));
        }
    }

    long getTotalWinnings(List<Bid> bids) {
        List<Bid> sorted = bids.stream().sorted(Comparator.comparing(bid -> bid.hand)).toList();

        return IntStream.range(0, sorted.size())
//                .peek(i -> System.out.println(sorted.get(i).hand.cards + ", " + sorted.get(i).bid + " ["+sorted.get(i).hand.getHandType()+"] : " + (i+1)))
                .map(index -> (index + 1) * sorted.get(index).bid)
                .sum();
    }


    @Override
    public void run() {
        List<Bid> testBids = list(getTestInputPath(), Bid::parse);
        System.out.println("Total Winnings of Test hands (6440) : " + getTotalWinnings(testBids));
        List<Bid> bids = list(getInputPath(), Bid::parse);
        System.out.println("Total Winnings of hands (255048101) : " + getTotalWinnings(bids));
    }
}
