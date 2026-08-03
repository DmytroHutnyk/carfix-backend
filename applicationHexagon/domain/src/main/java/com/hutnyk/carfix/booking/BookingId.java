package com.hutnyk.carfix.booking;

import com.hutnyk.carfix.util.Validator;
import java.util.Locale;
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

    public String reference() {
        return "BK-" + id.toString().substring(0, 8).toUpperCase(Locale.ROOT);
    }
}
