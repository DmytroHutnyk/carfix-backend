package com.hutnyk.carfix.booking;

import com.hutnyk.carfix.util.Validator;
import java.util.UUID;

public record BookingId(UUID id) {
    public BookingId {
        Validator.notNull(id, "id");
    }

    public static BookingId of(UUID id) {
        return new BookingId(id);
    }

    public static BookingId genId() {
        return new BookingId(UUID.randomUUID());
    }
}
