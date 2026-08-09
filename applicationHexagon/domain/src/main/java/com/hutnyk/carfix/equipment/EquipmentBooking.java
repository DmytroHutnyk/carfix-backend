package com.hutnyk.carfix.equipment;

import com.hutnyk.carfix.booking.BookingId;
import com.hutnyk.carfix.scheduling.TimeRange;
import com.hutnyk.carfix.util.Validator;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public final class EquipmentBooking {

    @EqualsAndHashCode.Include
    private final Integer id;

    private final TimeRange bookedTime;
    private final LocalDate date;
    private final Integer equipmentId;
    private final BookingId bookingId;

    @Builder
    private EquipmentBooking(
            Integer id, TimeRange bookedTime, LocalDate date, Integer equipmentId, BookingId bookingId) {
        this.id = id;
        this.bookedTime = Validator.notNull(bookedTime, "bookedTime");
        this.date = Validator.notNull(date, "date");
        this.equipmentId = Validator.notNull(equipmentId, "equipmentId");
        this.bookingId = Validator.notNull(bookingId, "bookingId");
    }

    public static EquipmentBooking of(
            Integer id, TimeRange bookedTime, LocalDate date, Integer equipmentId, BookingId bookingId) {
        return EquipmentBooking.builder()
                .id(id)
                .bookedTime(bookedTime)
                .date(date)
                .equipmentId(equipmentId)
                .bookingId(bookingId)
                .build();
    }
}
