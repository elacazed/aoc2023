package fr.ela.aoc2023;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class D19 extends AoC {

    static Pattern RULE_PATTERN = Pattern.compile("([amsx])([><])([0-9]+):([ARa-z]+)");
    static Pattern WORKFLOW_PATTERN = Pattern.compile("([a-z]+)\\{(.*)\\}");
    static Pattern PART_PATTERN = Pattern.compile("\\{x=([0-9]+),m=([0-9]+),a=([0-9]+),s=([0-9]+)\\}");

    public static Rule parseRule(String pred) {
        if (pred.matches("[RAa-z]+")) {
            return new Rule(x -> true, pred);
        }
        Matcher m = RULE_PATTERN.matcher(pred);
        if (m.matches()) {
            ToIntFunction<Part> getter = switch (m.group(1).charAt(0)) {
                case 'a' -> Part::a;
                case 'm' -> Part::m;
                case 'x' -> Part::x;
                case 's' -> Part::s;
                default -> throw new IllegalArgumentException();
            };
            char op = m.group(2).charAt(0);
            long val = Long.parseLong(m.group(3));
            String to = m.group(4);
            Predicate<Part> predicate = switch (op) {
                case '>' -> p -> getter.applyAsInt(p) > val;
                case '<' -> p -> getter.applyAsInt(p) < val;
                default -> throw new IllegalArgumentException();
            };
            return new Rule(predicate, to);
        }
        throw new IllegalArgumentException(pred);
    }

    public static Workflow parseWorkflow(String line) {
        Matcher m = WORKFLOW_PATTERN.matcher(line);
        if (m.matches()) {
            String name = m.group(1);
            String[] rules = m.group(2).split(",");
            return new Workflow(name, Arrays.stream(rules).map(D19::parseRule).toList());
        }
        throw new IllegalArgumentException(line);
    }

    public static Part parsePart(String line) {
        Matcher m = PART_PATTERN.matcher(line);
        if (m.matches()) {
            return new Part(Integer.parseInt(m.group(1)),Integer.parseInt(m.group(2)),Integer.parseInt(m.group(3)),Integer.parseInt(m.group(4)));
        }
        throw new IllegalArgumentException(line);
    }

    public record Part(int x, int m, int a, int s) {
        public long score() {
            return x + m + a + s;
        }
    }

    public record Rule(Predicate<Part> check, String to) implements Function<Part, String> {
        @Override
        public String apply(Part part) {
            return check.test(part) ? to : null;
        }
    }

    public record Workflow(String name, List<Rule> rules) {
        String out(Part in) {
            return rules.stream().map(r -> r.apply(in)).filter(Objects::nonNull).findFirst().orElse("A");
        }
    }

    public class Machine {
        private final Map<String, Workflow> workflows;

        public Machine(List<Workflow> workflows) {
            this.workflows = workflows.stream().collect(Collectors.toMap(w -> w.name, Function.identity()));
        }

        boolean accept(Part part) {
            Workflow w = workflows.get("in");
            String out = w.out(part);
            while (!out.equals("R")) {
                if (out.equals("A")) {
                    System.out.println(part+" accepted : "+part.score());
                    return true;
                }
                out = workflows.get(out).out(part);
            }
            //System.out.println(part+" rejected : "+part.score());
            return false;
        }
    }

    public long partOne(Machine machine, List<Part> parts) {
        return parts.stream().filter(machine::accept).mapToLong(Part::score).sum();
    }

    @Override
    public void run() {
        List<List<String>> testLines = splitOnEmptyLines(getTestInputPath());
        Machine testMachine = new Machine(testLines.get(0).stream().map(D19::parseWorkflow).toList());
        List<Part> testParts = testLines.get(1).stream().map(D19::parsePart).toList();

        System.out.println("Test part 1 (19114) : "+partOne(testMachine, testParts));

        List<List<String>> lines = splitOnEmptyLines(getInputPath());
        Machine machine = new Machine(lines.get(0).stream().map(D19::parseWorkflow).toList());
        List<Part> parts = lines.get(1).stream().map(D19::parsePart).toList();

        System.out.println("Part 1 (19114) : "+partOne(machine, parts));
    }
}

