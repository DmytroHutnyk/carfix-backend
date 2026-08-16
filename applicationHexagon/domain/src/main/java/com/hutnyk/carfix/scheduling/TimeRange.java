package com.hutnyk.carfix.scheduling;

import com.hutnyk.carfix.exception.DomainObjectValidationException;
import com.hutnyk.carfix.exception.ValidationErrorType;
import com.hutnyk.carfix.util.Validator;

import java.time.LocalDateTime;

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
}
