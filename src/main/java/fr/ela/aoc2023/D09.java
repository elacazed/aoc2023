package fr.ela.aoc2023;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class D09 extends AoC {

    public class Sequence {
        List<Long> values;

        public Sequence(String line) {
            values = Arrays.stream(line.split(" ")).map(Long::parseLong).toList();
        }

        long getNextHistoryValue() {
            Stack<Long> stack = new Stack<>();
            LinkedList<Long> ints = new LinkedList<>(values);
            while (! ints.stream().allMatch(i -> i ==0)) {
                stack.push(ints.getLast());
                ints = reduce(ints);
            }
            long res = 0;
            while ( ! stack.isEmpty()) {
                res = stack.pop() + res;
            }
            //System.out.println(" next value for "+values.stream().map(Object::toString).collect(Collectors.joining(", "))+" : "+res);
            return res;
        }

        LinkedList<Long> reduce(List<Long> ints) {
            LinkedList<Long> result = new LinkedList<>();
            for (int i=1; i < ints.size(); i++) {
                result.add(ints.get(i) - ints.get(i-1));
            }
            return result;
        }

    }

    @Override
    public void run() {
        long tres = list(getTestInputPath(), Sequence::new).stream().mapToLong(Sequence::getNextHistoryValue).sum();
        System.out.println("Test value part 1 (114) : "+tres);

        long res = list(getInputPath(), Sequence::new).stream().mapToLong(Sequence::getNextHistoryValue).sum();
        System.out.println("Test value part 1 (114) : "+res);
        //test result : 114
    }
}
