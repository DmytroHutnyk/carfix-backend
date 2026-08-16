package com.hutnyk.carfix.scheduling.mapper;

import com.hutnyk.carfix.exception.UnexpectedStateException;
import com.hutnyk.carfix.scheduling.TimeRange;
import io.hypersistence.utils.hibernate.type.range.Range;

import java.time.LocalDateTime;

public class TimeRangeMapper {

    public static TimeRange toDomain(Range<LocalDateTime> range) {
        if (range == null) return null;
        if (!range.isLowerBoundClosed() || range.isUpperBoundClosed()) {
            throw new UnexpectedStateException(
                    "Expected a half-open [lower,upper) range but got " + range.asString());
        }
        return TimeRange.of(range.lower(), range.upper());
    }

    public static Range<LocalDateTime> toRange(TimeRange timeRange) {
        if (timeRange == null) return null;
        return Range.closedOpen(timeRange.lower(), timeRange.upper());
    }
}
