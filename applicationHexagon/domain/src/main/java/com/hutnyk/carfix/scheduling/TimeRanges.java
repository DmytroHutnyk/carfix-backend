package com.hutnyk.carfix.scheduling;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class TimeRanges {

    private TimeRanges() {
    }

    public static List<TimeRange> union(List<TimeRange> ranges) {
        if (ranges.isEmpty()) {
            return List.of();
        }
        List<TimeRange> sorted = ranges.stream()
                .sorted(Comparator.comparing(TimeRange::lower))
                .toList();
        List<TimeRange> merged = new ArrayList<>();
        TimeRange current = sorted.getFirst();
        for (TimeRange next : sorted.subList(1, sorted.size())) {
            if (!next.lower().isAfter(current.upper())) {
                if (next.upper().isAfter(current.upper())) {
                    current = new TimeRange(current.lower(), next.upper());
                }
            } else {
                merged.add(current);
                current = next;
            }
        }
        merged.add(current);
        return List.copyOf(merged);
    }

    /**
     * Time present in BOTH lists. Each list is unioned first, so callers may pass raw,
     * unsorted, overlapping pieces. Result is merged and ascending; empty when nothing overlaps.
     */
    public static List<TimeRange> intersect(List<TimeRange> a, List<TimeRange> b) {
        List<TimeRange> mergedB = union(b);
        List<TimeRange> result = new ArrayList<>();
        for (TimeRange x : union(a)) {
            for (TimeRange y : mergedB) {
                x.intersect(y).ifPresent(result::add);
            }
        }
        // Both inputs are disjoint and ascending after union, so the pairwise overlaps come out
        // disjoint and ascending too — no second merge needed.
        return List.copyOf(result);
    }

    public static List<TimeRange> subtractAll(List<TimeRange> base, List<TimeRange> cuts) {
        List<TimeRange> result = union(base);
        for (TimeRange cut : cuts) {
            List<TimeRange> next = new ArrayList<>();
            for (TimeRange piece : result) {
                next.addAll(piece.subtract(cut));
            }
            result = next;
        }
        return List.copyOf(result);
    }

    public static List<TimeRange> free(List<TimeRange> availability, List<TimeRange> occupancy) {
        return subtractAll(availability, occupancy);
    }
}
