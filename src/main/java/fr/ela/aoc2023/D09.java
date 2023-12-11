package fr.ela.aoc2023;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

public class D09 extends AoC {

    public record Pair(long left, long right) {

        public Pair add(Pair other) {
            return new Pair(left+other.left, right+other.right);
        }
    }

    public class Sequence {
        List<Long> values;

        public Sequence(String line) {
            values = Arrays.stream(line.split(" ")).map(Long::parseLong).toList();
        }

        Pair getNextHistoryValue() {
            Stack<Pair> stack = new Stack<>();
            LinkedList<Long> ints = new LinkedList<>(values);
            while (!ints.stream().allMatch(i -> i == 0)) {
                stack.push(new Pair(ints.getFirst(), ints.getLast()));
                ints = reduce(ints);
            }
            Pair res = new Pair(0,0);
            while (!stack.isEmpty()) {
                Pair last = stack.pop();
                res = new Pair(last.left - res.left, last.right + res.right);
            }
            return res;
        }

        LinkedList<Long> reduce(List<Long> ints) {
            LinkedList<Long> result = new LinkedList<>();
            for (int i = 1; i < ints.size(); i++) {
                result.add(ints.get(i) - ints.get(i - 1));
            }

            return result;
        }

    }

    @Override
    public void run() {
        Pair test = list(getTestInputPath(), Sequence::new).stream().map(Sequence::getNextHistoryValue).reduce(new Pair(0,0), Pair::add);
        System.out.println("Test value part 1 (114) : " + test.right);
        System.out.println("Test value part 2 (2) : " + test.left);

        Pair result = list(getInputPath(), Sequence::new).stream().map(Sequence::getNextHistoryValue).reduce(new Pair(0,0), Pair::add);
        System.out.println("Result part 1 (1884768153) : " + result.right);
        System.out.println("Result part 2 (1031) : " + result.left);
    }
}
