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
