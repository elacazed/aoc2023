package fr.ela.aoc2023;

import fr.ela.aoc2023.utils.Range;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
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

    private static final int MAX = 4000;
    private static final int MIN = 1;

    public static Rule parseRule(String pred) {
        if (pred.matches("[RAa-z]+")) {
            return new Rule(x -> true, p -> new Pair(p, null), pred, '.', 0L);
        }
        Matcher m = RULE_PATTERN.matcher(pred);
        if (m.matches()) {
            char type = m.group(1).charAt(0);

            char op = m.group(2).charAt(0);
            long val = Long.parseLong(m.group(3));
            String to = m.group(4);
            Predicate<Part> predicate = buildPartPredicate(type, op, val);
            Function<Parts, Pair> partsSplitter = buildPartsSplitter(type, op, val);
            return new Rule(predicate, partsSplitter, to, op, val);
        }
        throw new IllegalArgumentException(pred);
    }

    private static Predicate<Part> buildPartPredicate(char type, char op, long val) {
        ToIntFunction<Part> getter = switch (type) {
            case 'a' -> Part::a;
            case 'm' -> Part::m;
            case 'x' -> Part::x;
            case 's' -> Part::s;
            default -> throw new IllegalArgumentException();
        };
        return switch (op) {
            case '>' -> p -> getter.applyAsInt(p) > val;
            case '<' -> p -> getter.applyAsInt(p) < val;
            default -> throw new IllegalArgumentException();
        };
    }

    private static Function<Parts, Pair> buildPartsSplitter(char type, char op, long val) {
        BiFunction<Parts, Function<Range, Range>, Parts> builder = switch (type) {
            case 'a' -> (p, f) -> new Parts(p.x(), p.m(), f.apply(p.a()), p.s());
            case 'm' -> (p, f) -> new Parts(p.x(), f.apply(p.m()), p.a(), p.s());
            case 'x' -> (p, f) -> new Parts(f.apply(p.x()), p.m(), p.a(), p.s());
            case 's' -> (p, f) -> new Parts(p.x(), p.m(), p.a(), f.apply(p.s()));
            default -> throw new IllegalArgumentException();
        };
        return switch (op) {
            case '>' -> parts -> new Pair(builder.apply(parts, r -> r.intersectionWith(new Range(val + 1, MAX))),
                    builder.apply(parts, r -> r.intersectionWith(new Range(MIN, val))));
            case '<' -> parts -> new Pair(builder.apply(parts, r -> r.intersectionWith(new Range(MIN, val - 1))),
                    builder.apply(parts, r -> r.intersectionWith(new Range(val, MAX))));
            default -> throw new IllegalArgumentException();
        };
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
            return new Part(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)), Integer.parseInt(m.group(3)), Integer.parseInt(m.group(4)));
        }
        throw new IllegalArgumentException(line);
    }

    public record Part(int x, int m, int a, int s) {
        public long score() {
            return x + m + a + s;
        }
    }

    public record Parts(Range x, Range m, Range a, Range s) {
        public long combinations() {
            return x.size() * m.size() * a.size() * s.size();
        }

        public boolean overlaps(Parts other) {
            return x.overlap(other.x) && m.overlap(other.m) && a.overlap(other.a) && s.overlap(other.s);
        }

    }

    public record Pair(Parts matches, Parts next) {

    }

    public record Rule(Predicate<Part> check, Function<Parts, Pair> splitter, String to, char op, long val) implements Function<Part, String> {
        @Override
        public String apply(Part part) {
            return check.test(part) ? to : null;
        }

        public Pair split(Parts parts) {
            return splitter.apply(parts);
        }

        public Parts apply(Parts parts, Map<String, List<Parts>> partsMap) {
            Pair pair = splitter.apply(parts);
            if (! to.equals("R")) {
                partsMap.computeIfAbsent(to, n -> new ArrayList<>()).add(pair.matches);
            }
            return pair.next;
        }
    }


    public record Workflow(String name, List<Rule> rules) {
        String out(Part in) {
            return rules.stream().map(r -> r.apply(in)).filter(Objects::nonNull).findFirst().orElse("A");
        }

        void through(Parts parts, Map<String, List<Parts>> partsMap) {
            Parts p = parts;
            for (Rule rule : rules) {
                if (p != null) {
                    p = rule.apply(p, partsMap);
                }
            }
            // p should always be null.
            if (p != null) {
                throw new IllegalArgumentException();
            }
        }

        void through(Map<String, List<Parts>> partsMap) {
            List<Parts> in = partsMap.remove(name);
            if (in == null || in.isEmpty()) {
                return;
            }
            in.forEach(p -> this.through(p, partsMap));
        }

    }

    public class Machine {
        private final Map<String, Workflow> workflows;

        public Machine(List<Workflow> workflows) {
            this.workflows = workflows.stream().collect(Collectors.toMap(w -> w.name, Function.identity()));
        }

        Long findCombinations() {
            Parts allParts = new Parts(new Range(MIN, MAX), new Range(MIN, MAX), new Range(MIN, MAX), new Range(MIN, MAX));
            Map<String, List<Parts>> parts = new HashMap<>();
            List<Parts> start = new ArrayList<>();
            start.add(allParts);
            parts.put("in", start);
            parts.put("A", new ArrayList<>());
            while (parts.size() > 1) {
                List<Workflow> wfs = parts.keySet().stream().filter(n -> ! n.equals("A")).map(workflows::get).toList();
                for (Workflow wf : wfs) {
                    wf.through(parts);
                }
            }
            List<Parts> ok = parts.get("A");
            for (Parts p : ok) {
                if (ok.stream().filter(pa -> ! pa.equals(p)).anyMatch(p::overlaps)) {
                    throw new IllegalStateException("Overlapping");
                }
            }
            return parts.get("A").stream().mapToLong(Parts::combinations).sum();
        }

        boolean accept(Part part) {
            Workflow w = workflows.get("in");
            String out = w.out(part);
            while (!out.equals("R")) {
                if (out.equals("A")) {
                    return true;
                }
                out = workflows.get(out).out(part);
            }
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

        System.out.println("Test part 1 (19114) : " + partOne(testMachine, testParts));
        System.out.println("Test Combinations (167409079868000) : "+testMachine.findCombinations());

        List<List<String>> lines = splitOnEmptyLines(getInputPath());
        Machine machine = new Machine(lines.get(0).stream().map(D19::parseWorkflow).toList());
        List<Part> parts = lines.get(1).stream().map(D19::parsePart).toList();

        System.out.println("Part 1 (374873) : " + partOne(machine, parts));
        System.out.println("Combinations (122112157518711) : "+machine.findCombinations());
    }
}

