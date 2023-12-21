package fr.ela.aoc2023.utils;

import java.util.Comparator;
import java.util.Optional;

public record Range(long start, long end) implements Comparable<Range> {

    public static Optional<Range> maybe(long start, long end) {
        if (end >= start) {
            return Optional.of(new Range(start, end));
        } else {
            return Optional.empty();
        }
    }

    public long size() {
        return end - start + 1;
    }

    public String toString() {
        return "[" + start + ".." + end + "]";
    }

    public boolean contains(long value) {
        return value >= start && value <= end;
    }

    public boolean contains(Range other) {
        return contains(other.start) && contains(other.end);
    }

    public boolean overlap(Range other) {
        return contains(other.start) || contains(other.end) || other.contains(start) || other.contains(end);
    }

    @Override
    public int compareTo(Range o) {
        return Comparator.comparingLong(Range::start).thenComparing(Range::end).compare(this, o);
    }

    public Range intersectionWith(Range other) {
        if (!this.overlap(other)) {
            return null;
        } else if (this.equals(other)) {
            return this;
        } else {
            return new Range(Math.max(this.start, other.start), Math.min(this.end, other.end));
        }
    }

    Range move(long offset) {
        return new Range(start + offset, end + offset);
    }
}