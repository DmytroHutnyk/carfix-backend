package com.hutnyk.carfix.booking;

import com.hutnyk.carfix.exception.DomainObjectValidationException;
import com.hutnyk.carfix.exception.ValidationErrorType;
import com.hutnyk.carfix.util.Validator;

import java.math.BigDecimal;
import java.time.LocalTime;

public record BookingSegment(Integer serviceId, LocalTime startTime, LocalTime endTime, BigDecimal price) {

    public BookingSegment {
        Validator.notNull(serviceId, "serviceId");
        Validator.validTimeRange(startTime, endTime, "segment");
        Validator.notNull(price, "price");
        if (price.signum() < 0) {
            throw new DomainObjectValidationException(ValidationErrorType.VALUE_OUT_OF_RANGE, "price", price);
        }
    }

    public static BookingSegment of(Integer serviceId, LocalTime startTime, LocalTime endTime, BigDecimal price) {
        return new BookingSegment(serviceId, startTime, endTime, price);
    }
}
