package fr.ela.aoc2023;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;
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

        public Reflection findReflection(Function<Integer, SymetryMatcher> matcherBuilder) {
            for (int i = 1; i < lines.size(); i++) {
                SymetryMatcher matcher = matcherBuilder.apply(i);
                boolean symetry = IntStream.range(0, Math.min(i, lines.size() - i))
                        .allMatch(offset -> matcher.test(offset, this));
                if (matcher.hasSymetry(symetry)) {
                    return new Reflection(axis, i);
                }
            }
            if (axis == Axis.VERTICAL) {
                throw new IllegalArgumentException("No symetry");
            }
            return rotate().findReflection(matcherBuilder);
        }

        public Reflection findReflection() {
            return findReflection(SymetryMatcher::new);
        }

        public Reflection findFudgeReflection() {
            return findReflection(FudgeMatcher::new);
        }

        boolean matches(int index, int offset) {
            return lines.get(index - offset - 1).equals(lines.get(index + offset));
        }

        boolean hasFudge(int index, int offset) {
            String one = lines.get(index - offset - 1);
            String two = lines.get(index + offset);
            return IntStream.range(0, one.length()).filter(i -> one.charAt(i) != two.charAt(i)).count() == 1;
        }
    }

    static class SymetryMatcher implements BiPredicate<Integer, LandPattern> {

        final int index;

        SymetryMatcher(int index) {
            this.index = index;
        }

        @Override
        public boolean test(Integer offset, LandPattern landPattern) {
            return landPattern.matches(index, offset);
        }

        boolean hasSymetry(boolean symetry) {
            return symetry;
        }
    }

    static class FudgeMatcher extends SymetryMatcher {
        boolean foundFudge = false;
        FudgeMatcher(int base) {
            super(base);
        }

        @Override
        public boolean test(Integer offset, LandPattern landPattern) {
            if (super.test(offset, landPattern)) {
                return true;
            }
            if (! foundFudge && landPattern.hasFudge(index, offset)) {
                foundFudge = true;
                return true;
            }
            return false;
        }

        boolean hasSymetry(boolean symetry) {
            return super.hasSymetry(symetry) && foundFudge;
        }
    }

    @Override
    public void run() {
        List<LandPattern> testPatterns = splitOnEmptyLines(getTestInputPath()).stream().map(LandPattern::new).toList();
        long testScore = testPatterns.stream().map(LandPattern::findReflection).mapToLong(Reflection::score).sum();
        System.out.println("Test score part 1 (405) : "+testScore);
        long testScore2 = testPatterns.stream().map(LandPattern::findFudgeReflection).mapToLong(Reflection::score).sum();
        System.out.println("Test score part 1 (400) : "+testScore2);

        List<LandPattern> patterns = splitOnEmptyLines(getInputPath()).stream().map(LandPattern::new).toList();
        long score = patterns.stream().map(LandPattern::findReflection).mapToLong(Reflection::score).sum();
        System.out.println("Score part 1 (27300) : "+score);
        long score2 = patterns.stream().map(LandPattern::findFudgeReflection).mapToLong(Reflection::score).sum();
        System.out.println("Score part 2 (29276) : "+score2);
    }


}
