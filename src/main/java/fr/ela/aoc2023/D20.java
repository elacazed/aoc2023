package fr.ela.aoc2023;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class D20 extends AoC {

    private static Pattern PATTERN = Pattern.compile("([&%]?[a-z]+) -> ([a-z, ]+)");


    public record Pulse(String from, boolean high, String to) {

        static Pulse of(String from, Boolean value, String to) {
            if (value == null) {
                return null;
            }
            return new Pulse(from, value, to);
        }

        public String toString() {
            return from + " -" + (high ? "high" : "low") + "-> " + to;
        }
    }

    public static Module parse(String line) {
        Matcher matcher = PATTERN.matcher(line);
        if (!matcher.matches()) {
            throw new IllegalArgumentException();
        }
        String[] targets = matcher.group(2).split(", ");
        String typeAndName = matcher.group(1);
        Module module = switch (typeAndName) {
            case "broadcaster" -> new BroadcasterModule();
            default -> switch (typeAndName.charAt(0)) {
                case '&' -> new Conjonction(typeAndName.substring(1));
                case '%' -> new FlipFLop(typeAndName.substring(1));
                default -> throw new IllegalArgumentException(line);
            };
        };
        for (String target : targets) {
            module.add(target);
        }
        return module;
    }

    public static abstract class Module implements Function<Pulse, List<Pulse>> {
        protected final String name;
        protected final List<String> next;

        protected Module(String name) {
            this.name = name;
            next = new ArrayList<>();
        }

        void add(String module) {
            this.next.add(module);
        }

        public String status() {
            return name + " -> " + String.join(", ", next);
        }

        abstract Boolean nextPulse(Pulse pulse);

        public List<Pulse> apply(Pulse pulse) {
            Boolean nextPulse = nextPulse(pulse);
            if (nextPulse != null) {
                return next.stream().map(n -> Pulse.of(name, nextPulse, n)).toList();
            } else {
                return List.of();
            }
        }

        public String name() {
            return name;
        }
    }

    public static class BroadcasterModule extends Module {
        protected BroadcasterModule() {
            super("broadcaster");
        }

        @Override
        public Boolean nextPulse(Pulse pulse) {
            return pulse.high;
        }
    }

    ;

    public static class Conjonction extends Module {
        private final Map<String, Boolean> pulseMap = new HashMap<>();

        public Conjonction(String name) {
            super(name);
        }

        //Conjunction modules (prefix &) remember the type of the most recent pulse received from each of their connected input modules;
        // they initially default to remembering a low pulse for each input.
        // When a pulse is received, the conjunction module first updates its memory for that input.
        // Then, if it remembers high pulses for all inputs, it sends a low pulse; otherwise, it sends a high pulse.
        @Override
        public Boolean nextPulse(Pulse pulse) {
            pulseMap.put(pulse.from, pulse.high);
            return pulseMap.values().stream().anyMatch(p -> !p);
        }

        public String status() {
            String status = next.stream().map(n -> n + "[" + (pulseMap.getOrDefault(n, Boolean.FALSE) ? "H" : "L") + "]")
                    .collect(Collectors.joining(","));
            return "&" + super.status() + " : " + status;
        }

        public void setInputs(List<String> strings) {
            strings.forEach(s -> pulseMap.put(s, Boolean.FALSE));
        }
    }


    public static class FlipFLop extends Module {
        //Flip-flop modules (prefix %) are either on or off;
        // they are initially off.
        // If a flip-flop module receives a high pulse, it is ignored and nothing happens.
        // However, if a flip-flop module receives a low pulse, it flips between on and off.
        // If it was off, it turns on and sends a high pulse.
        // If it was on, it turns off and sends a low pulse.
        private boolean status = false;

        protected FlipFLop(String name) {
            super(name);
        }

        @Override
        public Boolean nextPulse(Pulse pulse) {
            if (pulse.high) {
                return null;
            }
            Boolean out = !status;
            status = out;
            return out;
        }

        public String status() {
            return "%" + super.status() + " : " + status;
        }
    }

    record MachineState(String status, long high, long low) {

    }

    public static class Machine {
        final List<Module> modules;
        final Map<String, Module> modulesByName;
        final Module broadcaster;

        String getMachineStatus() {
            return modules.stream().map(Module::status).collect(Collectors.joining(" | "));
        }

        public Machine(List<String> lines) {
            modules = lines.stream().map(D20::parse).toList();
            modulesByName = modules.stream().collect(Collectors.toMap(Module::name, Function.identity()));
            Map<String, List<String>> moduleInputs = new HashMap<>();
            for (Module module : modules) {
                module.next.forEach(o -> moduleInputs.computeIfAbsent(o, m -> new ArrayList<>()).add(module.name));
            }
            for (Module module : modules) {
                if (module instanceof Conjonction conjonction) {
                    conjonction.setInputs(moduleInputs.get(module.name));
                }
            }

            broadcaster = modulesByName.get("broadcaster");
            Set<String> outputs = modules.stream().map(m -> m.next).flatMap(List::stream).collect(Collectors.toSet());
            outputs.removeAll(modulesByName.keySet());
            if (outputs.isEmpty()) {
                System.out.println("No outputs!");
            } else {
                System.out.println("Machine outputs : " + String.join(", ", outputs));
            }
        }

        Module getModule(String name) {
            return modulesByName.get(name);
        }

        public Pair push(int times) {
            Map<String, Integer> states = new HashMap<>();
            String initialState = getMachineStatus();
            states.put(initialState, 0);
            int i = 1;
            Pair total = push();
            String status = getMachineStatus();
            states.put(status, 1);
            while (!initialState.equals(status) && i < times) {
                i++;
                total = total.add(push());
                status = getMachineStatus();
            }
            if (i == times) {
                System.out.println("hahaha");
                return total;
            }
            int cycleLength = i;
            int cyclesNumber = times / cycleLength;
            int remaining = times % cycleLength;
            if (remaining > 0) {
                System.out.println("RESTE! ");
            }
            return total.multiply(cyclesNumber);

        }


        public Pair push() {
            Pulse p = new Pulse("button", false, "broadcaster");
            List<Pulse> result = process(p);
            return new Pair(0, 1).add(Pair.count(result));
        }

        public List<Pulse> process(Pulse p) {
            List<Pulse> accumulator = new ArrayList<>();
            List<Pulse> next = nextPulses(p);
            while (! next.isEmpty()) {
                accumulator.addAll(next);
                next = nextPulses(next);
            }
            return accumulator;
        }

        public List<Pulse> nextPulses(List<Pulse> pulses) {
            return pulses.stream().flatMap(p -> nextPulses(p).stream()).toList();
        }

        public List<Pulse> nextPulses(Pulse pulse) {
            return Optional.ofNullable(getModule(pulse.to)).map(m -> m.apply(pulse)).orElse(List.of());
        }
    }

    record Pair(long highs, long lows) {

        static Pair count(List<Pulse> pulses) {
            long total = pulses.size();
            long highs = pulses.stream().filter(Pulse::high).count();
            return new Pair(highs, total - highs);
        }

        Pair add(Pair other) {
            return new Pair(highs + other.highs, lows + other.lows);
        }

        Pair multiply(long times) {
            return new Pair(highs * times, lows * times);
        }

        long result() {
            return highs * lows;
        }
    }


    @Override
    public void run() {
        List<List<String>> testInputs = splitOnEmptyLines(getTestInputPath());
        Machine testMachine1 = new Machine(testInputs.get(0));
        Pair t1 = testMachine1.push(1000);
        System.out.println("Test 1 score (32000000) : " + t1.result());

        Machine testMachine2 = new Machine(testInputs.get(1));
        Pair t2 = testMachine2.push(1000);
        System.out.println("Test 2 score (11687500) : " + t2.result());

        Machine machine = new Machine(list(getInputPath()));
        Pair pair = machine.push(1000);
        System.out.println("Real Machine score (?) : " + pair.result());
    }
}

