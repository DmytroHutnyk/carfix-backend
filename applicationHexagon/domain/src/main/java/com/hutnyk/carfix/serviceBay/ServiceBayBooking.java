package com.hutnyk.carfix.serviceBay;

import com.hutnyk.carfix.booking.BookingId;
import com.hutnyk.carfix.scheduling.TimeRange;
import com.hutnyk.carfix.util.Validator;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public final class ServiceBayBooking {

    @EqualsAndHashCode.Include
    private final Integer id;

    private final TimeRange bookedTime;
    private final LocalDate date;
    private final Integer serviceBayId;
    private final BookingId bookingId;

    @Builder
    private ServiceBayBooking(
            Integer id, TimeRange bookedTime, LocalDate date, Integer serviceBayId, BookingId bookingId) {
        this.id = id;
        this.bookedTime = Validator.notNull(bookedTime, "bookedTime");
        this.date = Validator.notNull(date, "date");
        this.serviceBayId = Validator.notNull(serviceBayId, "serviceBayId");
        this.bookingId = Validator.notNull(bookingId, "bookingId");
    }

    public static ServiceBayBooking of(
            Integer id, TimeRange bookedTime, LocalDate date, Integer serviceBayId, BookingId bookingId) {
        return ServiceBayBooking.builder()
                .id(id)
                .bookedTime(bookedTime)
                .date(date)
                .serviceBayId(serviceBayId)
                .bookingId(bookingId)
                .build();
    }
}
