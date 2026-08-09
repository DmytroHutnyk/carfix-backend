package com.hutnyk.carfix.employee;

import com.hutnyk.carfix.booking.BookingId;
import com.hutnyk.carfix.scheduling.TimeRange;
import com.hutnyk.carfix.user.UserId;
import com.hutnyk.carfix.util.Validator;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public final class EmployeeBooking {

    @EqualsAndHashCode.Include
    private final Integer id;

    private final TimeRange bookedTime;
    private final LocalDate date;
    private final UserId employeeId;
    private final BookingId bookingId;

    @Builder
    private EmployeeBooking(
            Integer id, TimeRange bookedTime, LocalDate date, UserId employeeId, BookingId bookingId) {
        this.id = id;
        this.bookedTime = Validator.notNull(bookedTime, "bookedTime");
        this.date = Validator.notNull(date, "date");
        this.employeeId = Validator.notNull(employeeId, "employeeId");
        this.bookingId = Validator.notNull(bookingId, "bookingId");
    }

    public static EmployeeBooking of(
            Integer id, TimeRange bookedTime, LocalDate date, UserId employeeId, BookingId bookingId) {
        return EmployeeBooking.builder()
                .id(id)
                .bookedTime(bookedTime)
                .date(date)
                .employeeId(employeeId)
                .bookingId(bookingId)
                .build();
    }
}
