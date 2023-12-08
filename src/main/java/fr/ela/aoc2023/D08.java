package fr.ela.aoc2023;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class D08 extends AoC {

    private final Pattern PATTERN = Pattern.compile("([A-Z0-9]{3}) = \\(([A-Z0-9]{3}), ([A-Z0-9]{3})\\)");

    private class Plan {

        String directions;

        Map<String, String> left = new HashMap<>();
        Map<String, String> right = new HashMap<>();

        public Plan(List<String> list) {
            directions = list.get(0);
            list.stream().skip(2).forEach(l -> {
                Matcher m = PATTERN.matcher(l);
                if (m.matches()) {
                    left.put(m.group(1), m.group(2));
                    right.put(m.group(1), m.group(3));
                } else {
                    throw new IllegalArgumentException(l);
                }
            });
        }

        String next(String pos, char direction) {
            return switch (direction) {
                case 'L' -> left.get(pos);
                case 'R' -> right.get(pos);
                default -> throw new IllegalArgumentException(directions);
            };
        }

        long countStepsToZZZ() {
            return countStepsToUntil("AAA", "ZZZ"::equals);
        }

        int countStepsToUntil(String start, Predicate<String> stop) {
            int count = 0;
            int size = directions.length();
            String next = start;
            while (! stop.test(next)) {
                next = next(next, directions.charAt((count % size)));
                count++;
            }
            return count;
        }

        long countGhostsSteps() {
            Predicate<String> stop = s -> s.charAt(2) == 'Z';
            List<String> start = right.keySet().stream().filter(s -> s.charAt(2) == 'A').toList();

            BigInteger result = start.stream().map(s -> countStepsToUntil(s, stop)).map(BigInteger::valueOf)
                    .reduce(D08::lcm).orElseThrow();
            return result.longValue();
        }

    }

    public static BigInteger lcm(BigInteger number1, BigInteger number2) {
        BigInteger gcd = number1.gcd(number2);
        BigInteger absProduct = number1.multiply(number2).abs();
        return absProduct.divide(gcd);
    }

    @Override
    public void run() {
        Plan testPlan = new Plan(list(getTestInputPath()));
        System.out.println("Test plan : "+ testPlan.countStepsToZZZ()+" steps to reach ZZZ (2)");
        Plan testPlan2 = new Plan(list(getPath("input-test2")));
        System.out.println("Test plan : "+ testPlan2.countGhostsSteps()+" ghosts steps to reach Z (2)");
        Plan plan = new Plan(list(getInputPath()));
        System.out.println("Plan : "+ plan.countStepsToZZZ()+" steps to reach ZZZ (17263)");
        System.out.println("Plan : "+ plan.countGhostsSteps()+" ghosts to reach **Z (14631604759649)");
    }
}
