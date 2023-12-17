package fr.ela.aoc2023;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.IntStream;

public class D15 extends AoC {


    //Determine the ASCII code for the current character of the string.
    //Increase the current label by the ASCII code you just determined.
    //Set the current label to itself multiplied by 17.
    //Set the current label to the remainder of dividing itself by 256.

    static Function<Integer, Integer> chash(int c) {
        return v -> (((v + c) * 17) % 256);
    }

    static int hash(String sequence) {
        return sequence.chars().mapToObj(D15::chash)
                .reduce((f1, f2) -> f2.compose(f1))
                .map(f -> f.apply(0))
                .orElseThrow();
    }

    public record Lens(String label, int hash, AtomicInteger focal) {
        public static Lens parse(String value) {
            String[] vals = value.split("=");

            return new Lens(vals[0], D15.hash(vals[0]), new AtomicInteger(Integer.parseInt(vals[1])));
        }

        public void setFocal(int focalValue) {
            this.focal.set(focalValue);
        }
    }

    public class Boxes {
        Map<Integer, List<Lens>> map = new HashMap<>();

        public void add(Lens lens) {
            List<Lens> ll = map.computeIfAbsent(lens.hash, i -> new ArrayList<>());
            ll.stream()
                    .filter(l -> l.label.equals(lens.label))
                    .findFirst()
                    .ifPresentOrElse(l -> l.setFocal(lens.focal.intValue()),
                            () -> ll.add(lens));
        }

        public void remove(String label) {
            int hash = D15.hash(label);
            if (map.containsKey(hash)) {
                map.get(hash).removeIf(l -> label.equals(l.label));
            }
        }

        public void compute(String command) {
            if (command.endsWith("-")) {
                remove(command.substring(0, command.length() - 1));
            } else {
                add(Lens.parse(command));
            }
        }

        public long focusingPower() {
            //To confirm that all of the lenses are installed correctly,
            // add up the focusing power of all of the lenses.
            //
            //The focusing power of a single lens is the result of multiplying together:
            //One plus the box number of the lens in question.
            //The slot number of the lens within the box: 1 for the first lens, 2 for the second lens, and so on.
            //The focal length of the lens.
            return map.entrySet().stream().mapToLong(e -> focusingPower(e.getKey() + 1, e.getValue())).sum();
        }

        private long focusingPower(int hash, List<Lens> lenses) {
            return IntStream.range(0, lenses.size())
                    .mapToLong(i -> lenses.get(i).focal.intValue() * (i + 1) * hash)
                    .sum();
        }

    }

    public long part1(String value) {
        return Arrays.stream(value.split(",")).mapToLong(D15::hash).sum();
    }

    public Boxes parse(String value) {
        Boxes boxes = new Boxes();
        Arrays.stream(value.split(",")).forEach(boxes::compute);
        return boxes;
    }

    @Override
    public void run() {
        System.out.println("Test Initialisation sequence hash (1320): " + part1(readFile(getTestInputPath())));
        Boxes testBoxes = parse(readFile(getTestInputPath()));
        System.out.println("Test Focusing Power (145): " + testBoxes.focusingPower());

        System.out.println("Initialisation sequence hash (507666): " + part1(readFile(getInputPath())));
        Boxes boxes = parse(readFile(getInputPath()));
        System.out.println("Focusing Power (233537): " + boxes.focusingPower());

    }
}

