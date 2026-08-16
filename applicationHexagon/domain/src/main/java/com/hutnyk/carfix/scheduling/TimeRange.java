package com.hutnyk.carfix.scheduling;

import com.hutnyk.carfix.exception.DomainObjectValidationException;
import com.hutnyk.carfix.exception.ValidationErrorType;
import com.hutnyk.carfix.util.Validator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record TimeRange(LocalDateTime lower, LocalDateTime upper) {
    public TimeRange {
        Validator.notNull(lower, "lower");
        Validator.notNull(upper, "upper");
        if (!lower.isBefore(upper)) {
            throw new DomainObjectValidationException(
                    ValidationErrorType.INVALID_TIME_RANGE, "timeRange", lower + " - " + upper);
        }
    }

    public static TimeRange of(LocalDateTime lower, LocalDateTime upper) {
        return new TimeRange(lower, upper);
    }

    public boolean overlaps(TimeRange other) {
        return lower.isBefore(other.upper) && other.lower.isBefore(upper);
    }

    public boolean contains(TimeRange other) {
        return !lower.isAfter(other.lower) && !upper.isBefore(other.upper);
    }

    public boolean contains(LocalDateTime instant) {
        return !instant.isBefore(lower) && instant.isBefore(upper);
    }

    public Optional<TimeRange> intersect(TimeRange other) {
        if (!overlaps(other)) {
            return Optional.empty();
        }
        LocalDateTime maxLower = lower.isAfter(other.lower) ? lower : other.lower;
        LocalDateTime minUpper = upper.isBefore(other.upper) ? upper : other.upper;
        return Optional.of(new TimeRange(maxLower, minUpper));
    }

    public List<TimeRange> subtract(TimeRange other) {
        if (!overlaps(other)) {
            return List.of(this);
        }
        List<TimeRange> remainder = new ArrayList<>(2);
        if (lower.isBefore(other.lower)) {
            remainder.add(new TimeRange(lower, other.lower));
        }
        if (other.upper.isBefore(upper)) {
            remainder.add(new TimeRange(other.upper, upper));
        }
        return List.copyOf(remainder);
    }
}
