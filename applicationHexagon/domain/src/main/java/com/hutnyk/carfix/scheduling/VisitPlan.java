package com.hutnyk.carfix.scheduling;

import java.time.LocalDateTime;
import java.util.List;

public record VisitPlan(Integer bayId, List<SegmentPlan> segments) {
    public VisitPlan {
        segments = List.copyOf(segments);
    }

    public LocalDateTime start() {
        return segments.getFirst().time().lower();
    }

    public LocalDateTime end() {
        return segments.getLast().time().upper();
    }
}
