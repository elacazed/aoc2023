package fr.ela.aoc2023;

import java.util.Arrays;
import java.util.function.Function;

public class D15 extends AoC {


    //Determine the ASCII code for the current character of the string.
    //Increase the current value by the ASCII code you just determined.
    //Set the current value to itself multiplied by 17.
    //Set the current value to the remainder of dividing itself by 256.

    Function<Integer, Integer> chash(int c) {
        return v -> (((v + c) * 17) % 256);
    }

    int hash(String sequence) {
        return sequence.chars().mapToObj(this::chash)
                .reduce((f1, f2) -> f2.compose(f1))
                .map(f -> f.apply(0))
                .orElseThrow();
    }

    public long part1(String value) {
        return Arrays.stream(value.split(",")).mapToLong(this::hash).sum();
    }

    @Override
    public void run() {
        System.out.println("Test Initialisation sequence hash (1320): "+part1(readFile(getTestInputPath())));

        System.out.println("Initialisation sequence hash (507666): "+part1(readFile(getInputPath())));
    }
}

