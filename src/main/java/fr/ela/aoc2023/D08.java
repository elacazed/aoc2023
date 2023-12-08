package fr.ela.aoc2023;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class D08 extends AoC {

    private final Pattern PATTERN = Pattern.compile("([A-Z]{3}) = \\(([A-Z]{3}), ([A-Z]{3})\\)");

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

        int countStepsToZZZ() {
            int count = 0;
            int size = directions.length();
            String next = "AAA";
            while (!"ZZZ".equals(next)) {
                next = next(next, directions.charAt((count % size)));
                count++;
            }
            return count;
        }

    }

    @Override
    public void run() {
        Plan testPlan = new Plan(list(getTestInputPath()));
        System.out.println("Test plan : "+ testPlan.countStepsToZZZ()+" steps to reach ZZZ (2)");

        Plan plan = new Plan(list(getInputPath()));
        System.out.println("Plan : "+ plan.countStepsToZZZ()+" steps to reach ZZZ (17263)");
    }
}
