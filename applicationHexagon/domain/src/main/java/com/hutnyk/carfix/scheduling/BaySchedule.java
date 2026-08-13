package com.hutnyk.carfix.scheduling;

import java.util.List;

public record BaySchedule(Integer bayId, Integer bayTypeId, List<TimeRange> free) {
    public BaySchedule {
        free = TimeRanges.union(free);
    }
}
