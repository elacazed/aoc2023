package fr.ela.aoc2023;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

public class D07 extends AoC {

    public record Hand(String cards, Map<Character, Integer> buckets) implements Comparable<Hand> {

        public Hand(String cards) {
            this(cards, new HashMap<>());
            fillBuckets();
        }

        public Hand withJokers() {
            return new Hand(cards.replace('J', '*'));
        }

        private void fillBuckets() {
            for (char c : cards.toCharArray()) {
                buckets.merge(c, 1, (value, count) -> value + 1);
            }
        }

        HandType getHandType() {
            Map<Character, Integer> bucks = new HashMap<>(buckets);
            int jokers = Optional.ofNullable(bucks.remove('*')).orElse(0);
            int cards = cards().length() - jokers;
            return switch (bucks.size()) {
                // 0 groups -> 5 jokers. 1 group -> all same cards + jokers.
                case 0,1 -> HandType.FIVE_OF_A_KIND;
                // 2 groups :
                //     - 5 cards : [4/1, 3/2],
                //     - 4 cards : [3/1, 2/2] + 1 joker
                //     - 3 cards [2/1] + 2 jokers
                //     - 2 cards [1/1] + 3 jokers
                // If a group contains only 1 card, adding jokers to the other group makes 4 cards, otherwise a FULL_HOUSE.
                case 2 -> bucks.containsValue(1) ? HandType.FOUR_OF_A_KIND : HandType.FULL_HOUSE;
                // 3 groups :
                //     - 5 cards : [3/1/1, 2/2/1]. if no group of 3 => two pairs, otherwise three of a kind
                //     - 4 cards : [2/1/1] + 1 joker => three of a kind
                //     - 3 cards [1/1/1] + 2 jokers => three of a kind
                case 3 -> cards == 5 && ! bucks.containsValue(3) ? HandType.TWO_PAIR : HandType.THREE_OF_A_KIND;
                // 4 groups :
                //     - 5 cards : 2/1/1/1
                //     - 4 cards : 1/1/1/1 + 1 joker
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
                case '*' -> 1;
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

        public static Bid withJokers(Bid bid) {
            return new Bid(bid.hand.withJokers(), bid.bid());
        }

    }

    long getTotalWinnings(List<Bid> bids) {
        List<Bid> sorted = bids.stream().sorted(Comparator.comparing(bid -> bid.hand)).toList();

        return IntStream.range(0, sorted.size())
                .map(index -> (index + 1) * sorted.get(index).bid)
                .sum();
    }


    @Override
    public void run() {
        List<Bid> testBids = list(getTestInputPath(), Bid::parse);
        System.out.println("Total Winnings of Test hands (6440) : " + getTotalWinnings(testBids));
        List<Bid> testBidsWithJokers = testBids.stream().map(Bid::withJokers).toList();
        System.out.println("Total Winnings of Test hands with Jokers (5905) : " + getTotalWinnings(testBidsWithJokers));
        List<Bid> bids = list(getInputPath(), Bid::parse);
        System.out.println("Total Winnings of hands (255048101) : " + getTotalWinnings(bids));
        List<Bid> bidsWithJokers = bids.stream().map(Bid::withJokers).toList();
        System.out.println("Total Winnings of hands (253718286) : " + getTotalWinnings(bidsWithJokers));
    }
}
