package fr.ela.aoc2023;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class D13 extends AoC {

    public enum Axis {
        HORIZONTAL, VERTICAL;
    }

    record Reflection(Axis axis, int position) {
        long score() {
            return (long) (axis == Axis.HORIZONTAL ? 100 : 1) * position;
        }
    }

    record LandPattern(List<String> lines, Axis axis) {

        public LandPattern(List<String> lines) {
            this(lines, Axis.HORIZONTAL);
        }

        LandPattern rotate() {
            int length = lines.get(0).length();
            List<String> rot = new ArrayList<>(length);
            for (int i = 0; i < length; i++) {
                char[] chars = new char[lines.size()];
                for (int j = 0; j < lines.size(); j++) {
                    chars[j] = lines.get(j).charAt(i);
                }
                rot.add(new String(chars));
            }
            return new LandPattern(rot, Axis.VERTICAL);
        }

        public Reflection findReflection() {
            for (int i = 1; i < lines.size(); i++) {
                final int index = i;
                boolean symetry = IntStream.range(0, Math.min(index, lines.size() - index))
                        .allMatch(offset -> matches(index, offset));
                if (symetry) {
                    return new Reflection(axis, i);
                }
            }
            if (axis == Axis.VERTICAL) {
                throw new IllegalArgumentException("No symetry");
            }
            return rotate().findReflection();
        }

        boolean matches(int index, int offset) {
            return lines.get(index - offset - 1).equals(lines.get(index + offset));
        }

    }

    @Override
    public void run() {
        List<LandPattern> testPatterns = splitOnEmptyLines(getTestInputPath()).stream().map(LandPattern::new).toList();
        long testScore = testPatterns.stream().map(LandPattern::findReflection).mapToLong(Reflection::score).sum();
        System.out.println("Test score part 1 (405) : "+testScore);


        List<LandPattern> patterns = splitOnEmptyLines(getInputPath()).stream().map(LandPattern::new).toList();
        long score = patterns.stream().map(LandPattern::findReflection).mapToLong(Reflection::score).sum();
        System.out.println("Score part 1 (27300) : "+score);
    }


}
