package fr.ela.aoc2023;

import java.math.BigInteger;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
                case '%' -> new FlipFlop(typeAndName.substring(1));
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

        public void setInputs(List<String> strings) {
            strings.forEach(s -> pulseMap.put(s, Boolean.FALSE));
        }
        public String toString() {
            return "Conjonction [" + name + "] " + String.join(", ", next);
        }
    }


    public static class FlipFlop extends Module {
        //Flip-flop modules (prefix %) are either on or off;
        // they are initially off.
        // If a flip-flop module receives a high pulse, it is ignored and nothing happens.
        // However, if a flip-flop module receives a low pulse, it flips between on and off.
        // If it was off, it turns on and sends a high pulse.
        // If it was on, it turns off and sends a low pulse.
        private boolean status = false;

        protected FlipFlop(String name) {
            super(name);
        }
        public String toString() {
            return "FlipFlop ["+name+"] "+String.join(", ", next);
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

    }

    public static class Machine {
        final List<Module> modules;
        final Map<String, Module> modulesByName;
        final Module broadcaster;

        public static Machine build(List<String> lines) {
            return new Machine(lines.stream().map(D20::parse).toList());
        }

        public Machine(List<Module> modules) {
            this.modules = modules;
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
        }


        List<Module> getNext(Module source) {
            return source.next.stream().map(modulesByName::get).toList();
        }


        // The input of rx is a single Conjonction, which has 4 conjonction as inputs.
        // We know there is a cycle in the machine state => we must find the LCM of the number of pushes each of these needs to receive a single low pulse to the input of rx
        // At this point, input(rx) will have received 4 low pulses,
        // We push the button until each one receives a single low => on PPCM
        // each conjonction -> RX.
        private Long pushUntilSingleLowPulseToRX() {
            // Needs to have received only high pulses => sends a low pulse to rx.
            Conjonction inputOfRx = modules.stream().filter(m -> m.next.contains("rx")).findFirst().map(Conjonction.class::cast).orElseThrow();
            List<Conjonction> inputsOfInputOfRx = modules.stream().filter(m -> m.next.contains(inputOfRx.name)).map(Conjonction.class::cast).toList();

            Map<Module, Integer> map = new HashMap<>();
            int count = 0;
            // We click until we each input of the input of rx sends a high pulse => there is at least one low pulse in its inputs.
            while (map.size() < inputsOfInputOfRx.size()) {
                List<Pulse> pulses = push();
                count++;
                for (Module target : inputsOfInputOfRx) {
                    Long nb = pulses.stream().filter(p -> !p.high && p.to.equals(target.name)).count();
                    if (nb != 0) {
                        map.put(target, count);
                    }
                }
            }
            return map.values().stream().mapToLong(Integer::intValue).reduce((x, y) -> x * y).orElseThrow();
        }

        Module getModule(String name) {
            return modulesByName.get(name);
        }

        public Pair push(int times) {
            Pair total = new Pair(0, 0);
            for (int i = 0; i < times; i++) {
                total = total.add(Pair.count(push()));
            }
            return total;
        }

        public List<Pulse> push() {
            List<Pulse> result = new ArrayList<>();
            Pulse p = new Pulse("button", false, "broadcaster");
            result.add(p);
            result.addAll(process(p));
            return result;
        }

        public List<Pulse> process(Pulse p) {
            List<Pulse> accumulator = new ArrayList<>();
            List<Pulse> next = nextPulses(p);
            while (!next.isEmpty()) {
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


        /* ------------ Solution found on Reddit : astonishing analysis! --- */

        private Long verySmartVersionWithBinaryRepresentationOfNumber() {
            List<Integer> loopCycles = new ArrayList<>();
            for (Module dest : getNext(broadcaster)) {
                List<Module> group = getBinaryCounterGroup(dest);
                loopCycles.add(buildBinaryString(group));
            }
            BigInteger nbPushes = BigInteger.ONE;
            for (int cycle : loopCycles) {
                BigInteger bg = BigInteger.valueOf(cycle);
                BigInteger gcd = nbPushes.gcd(bg);
                BigInteger absProduct = nbPushes.multiply(bg).abs();
                nbPushes = absProduct.divide(gcd);
            }
            return nbPushes.longValue();
        }

        private List<Module> getBinaryCounterGroup(Module source) {
            List<Module> group = new ArrayList<>();
            group.add(source);
            for (Module destination : getNext(source)) {
                if (destination instanceof FlipFlop flipflop) {
                    group.addAll(getBinaryCounterGroup(flipflop));
                }
            }
            return group;
        }

        private int buildBinaryString(List<Module> group) {
            int size = group.size();
            char[] digits = new char[size];
            for (int i = 0; i < size; i++) {
                digits[size - i - 1] = getNext(group.get(i)).stream().anyMatch(m -> m instanceof Conjonction) ? '1' : '0';
            }
            return Integer.parseInt(new String(digits), 2);
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
        Machine testMachine1 = Machine.build(testInputs.get(0));
        Pair t1 = testMachine1.push(1000);
        System.out.println("Test 1 score (32000000) : " + t1.result());

        Machine testMachine2 = Machine.build(testInputs.get(1));
        Pair t2 = testMachine2.push(1000);
        System.out.println("Test 2 score (11687500) : " + t2.result());

        Machine machine = Machine.build(list(getInputPath()));
        Pair pair = machine.push(1000);
        System.out.println("Real Machine score (730797576) : " + pair.result());

        machine = Machine.build(list(getInputPath()));
        long time = System.nanoTime();
        System.out.println("Number of pushes to get a single low pulse to RX (226732077152351) : " + machine.pushUntilSingleLowPulseToRX()+" ("+ formatDuration(Duration.ofNanos(System.nanoTime() - time))+")");
        machine = Machine.build(list(getInputPath()));
        time = System.nanoTime();
        System.out.println("Clever Solution (226732077152351) : " + machine.verySmartVersionWithBinaryRepresentationOfNumber()+" ("+ formatDuration(Duration.ofNanos(System.nanoTime() - time))+")");

    }

}

